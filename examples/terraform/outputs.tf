# Shared outputs
output "ecr_repository_url" {
  description = "ECR repository URL"
  value       = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.region}.amazonaws.com/${lower(var.app_name)}"
}

# IAM-based runtime outputs
output "iam_agent_runtime_arn" {
  description = "IAM AgentCore runtime ARN"
  value       = aws_bedrockagentcore_agent_runtime.iam.agent_runtime_arn
}

output "iam_agent_runtime_id" {
  description = "IAM AgentCore runtime ID"
  value       = aws_bedrockagentcore_agent_runtime.iam.agent_runtime_id
}

output "iam_role_arn" {
  description = "IAM role ARN for IAM runtime"
  value       = aws_iam_role.agentcore_runtime_iam.arn
}

# OAuth2-based runtime outputs
output "oauth_agent_runtime_arn" {
  description = "OAuth2 AgentCore runtime ARN"
  value       = aws_bedrockagentcore_agent_runtime.oauth.agent_runtime_arn
}

output "oauth_agent_runtime_id" {
  description = "OAuth2 AgentCore runtime ID"
  value       = aws_bedrockagentcore_agent_runtime.oauth.agent_runtime_id
}

output "oauth_role_arn" {
  description = "IAM role ARN for OAuth2 runtime"
  value       = aws_iam_role.agentcore_runtime_oauth.arn
}

# Cognito outputs for OAuth2
output "cognito_user_pool_id" {
  description = "Cognito User Pool ID"
  value       = aws_cognito_user_pool.app.id
}

output "cognito_client_id" {
  description = "Cognito User Pool Client ID"
  value       = aws_cognito_user_pool_client.app.id
}

output "cognito_domain" {
  description = "Cognito domain URL"
  value       = "https://${aws_cognito_user_pool_domain.app.domain}.auth.${var.region}.amazoncognito.com"
}

output "oidc_discovery_url" {
  description = "OpenID Connect discovery URL"
  value       = "https://cognito-idp.${var.region}.amazonaws.com/${aws_cognito_user_pool.app.id}/.well-known/openid-configuration"
}
