data "aws_caller_identity" "current" {}

# Generate random suffix for globally unique domain
resource "random_id" "domain_suffix" {
  byte_length = 4
}

locals {
  image_tag = fileexists("image-version.txt") ? trimspace(file("image-version.txt")) : var.image_tag
}

# Cognito User Pool for OAuth2
resource "aws_cognito_user_pool" "app" {
  name = "${var.app_name}-user-pool"

  password_policy {
    minimum_length    = 8
    require_lowercase = true
    require_numbers   = true
    require_symbols   = false
    require_uppercase = true
  }

  auto_verified_attributes = ["email"]
}

# Cognito User Pool Client for OAuth2
resource "aws_cognito_user_pool_client" "app" {
  name         = "${var.app_name}-client"
  user_pool_id = aws_cognito_user_pool.app.id

  generate_secret                      = false
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]
  supported_identity_providers         = ["COGNITO"]
  callback_urls                        = ["http://localhost:3000/callback"]
  logout_urls                          = ["http://localhost:3000/logout"]

  explicit_auth_flows = [
    "ALLOW_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_ADMIN_USER_PASSWORD_AUTH"
  ]
}

# Cognito User Pool Domain for OAuth2
resource "aws_cognito_user_pool_domain" "app" {
  domain       = "${lower(var.app_name)}-${random_id.domain_suffix.hex}"
  user_pool_id = aws_cognito_user_pool.app.id
}

# IAM Role for IAM-based AgentCore Runtime
resource "aws_iam_role" "agentcore_runtime_iam" {
  name = "${var.app_name}IamAgentCoreRuntimeRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AssumeRolePolicy"
        Effect = "Allow"
        Principal = {
          Service = "bedrock-agentcore.amazonaws.com"
        }
        Action = "sts:AssumeRole"
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = data.aws_caller_identity.current.account_id
          }
          ArnLike = {
            "aws:SourceArn" = "arn:aws:bedrock-agentcore:${var.region}:${data.aws_caller_identity.current.account_id}:*"
          }
        }
      }
    ]
  })
}

# IAM Role for OAuth2-based AgentCore Runtime
resource "aws_iam_role" "agentcore_runtime_oauth" {
  name = "${var.app_name}OauthAgentCoreRuntimeRole"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AssumeRolePolicy"
        Effect = "Allow"
        Principal = {
          Service = "bedrock-agentcore.amazonaws.com"
        }
        Action = "sts:AssumeRole"
        Condition = {
          StringEquals = {
            "aws:SourceAccount" = data.aws_caller_identity.current.account_id
          }
          ArnLike = {
            "aws:SourceArn" = "arn:aws:bedrock-agentcore:${var.region}:${data.aws_caller_identity.current.account_id}:*"
          }
        }
      }
    ]
  })
}

# Shared IAM Policy for both runtimes
locals {
  shared_policy = {
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "ECRImageAccess"
        Effect = "Allow"
        Action = [
          "ecr:BatchGetImage",
          "ecr:GetDownloadUrlForLayer"
        ]
        Resource = [
          "arn:aws:ecr:${var.region}:${data.aws_caller_identity.current.account_id}:repository/${lower(var.app_name)}"
        ]
      },
      {
        Sid    = "ECRTokenAccess"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Effect = "Allow"
        Action = [
          "logs:DescribeLogStreams",
          "logs:CreateLogGroup",
          "logs:CreateLogStream",
          "logs:PutLogEvents"
        ]
        Resource = [
          "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:/aws/bedrock-agentcore/runtimes/*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "logs:DescribeLogGroups"
        ]
        Resource = [
          "arn:aws:logs:${var.region}:${data.aws_caller_identity.current.account_id}:log-group:*"
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "xray:PutTraceSegments",
          "xray:PutTelemetryRecords",
          "xray:GetSamplingRules",
          "xray:GetSamplingTargets"
        ]
        Resource = ["*"]
      },
      {
        Effect = "Allow"
        Resource = "*"
        Action = "cloudwatch:PutMetricData"
        Condition = {
          StringEquals = {
            "cloudwatch:namespace" = "bedrock-agentcore"
          }
        }
      },
      {
        Sid = "GetAgentAccessToken"
        Effect = "Allow"
        Action = [
          "bedrock-agentcore:GetWorkloadAccessToken",
          "bedrock-agentcore:GetWorkloadAccessTokenForJWT",
          "bedrock-agentcore:GetWorkloadAccessTokenForUserId"
        ]
        Resource = [
          "arn:aws:bedrock-agentcore:${var.region}:${data.aws_caller_identity.current.account_id}:workload-identity-directory/default",
          "arn:aws:bedrock-agentcore:${var.region}:${data.aws_caller_identity.current.account_id}:workload-identity-directory/default/workload-identity/${var.app_name}-*"
        ]
      },
      {
        Sid = "BedrockModelInvocation"
        Effect = "Allow"
        Action = [
          "bedrock:InvokeModel",
          "bedrock:InvokeModelWithResponseStream"
        ]
        Resource = [
          "arn:aws:bedrock:*::foundation-model/*",
          "arn:aws:bedrock:${var.region}:${data.aws_caller_identity.current.account_id}:*",
          "arn:aws:bedrock:*:${data.aws_caller_identity.current.account_id}:inference-profile/*"
        ]
      }
    ]
  }
}

# IAM Policy for IAM-based runtime
resource "aws_iam_role_policy" "agentcore_execution_iam" {
  name   = "AgentCoreExecutionPolicy"
  role   = aws_iam_role.agentcore_runtime_iam.id
  policy = jsonencode(local.shared_policy)
}

# IAM Policy for OAuth2-based runtime
resource "aws_iam_role_policy" "agentcore_execution_oauth" {
  name   = "AgentCoreExecutionPolicy"
  role   = aws_iam_role.agentcore_runtime_oauth.id
  policy = jsonencode(local.shared_policy)
}

# IAM-based AgentCore Runtime
resource "aws_bedrockagentcore_agent_runtime" "iam" {
  agent_runtime_name = "${var.app_name}Iam"
  role_arn          = aws_iam_role.agentcore_runtime_iam.arn

  agent_runtime_artifact {
    container_configuration {
      container_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com/${lower(var.app_name)}:${local.image_tag}"
    }
  }

  network_configuration {
    network_mode = "PUBLIC"
  }

  request_header_configuration {
    request_header_allowlist = ["X-Amzn-Bedrock-AgentCore-Runtime-Custom-Test"]
  }
}

# OAuth2-based AgentCore Runtime
resource "aws_bedrockagentcore_agent_runtime" "oauth" {
  agent_runtime_name = "${var.app_name}Oauth"
  role_arn          = aws_iam_role.agentcore_runtime_oauth.arn

  agent_runtime_artifact {
    container_configuration {
      container_uri = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com/${lower(var.app_name)}:${local.image_tag}"
    }
  }

  network_configuration {
    network_mode = "PUBLIC"
  }

  authorizer_configuration {
    custom_jwt_authorizer {
      discovery_url    = "https://cognito-idp.${var.region}.amazonaws.com/${aws_cognito_user_pool.app.id}/.well-known/openid-configuration"
      allowed_clients  = [aws_cognito_user_pool_client.app.id]
    }
  }

  request_header_configuration {
    request_header_allowlist = [
      "Authorization",
      "X-Amzn-Bedrock-AgentCore-Runtime-Custom-Test"
    ]
  }
}
