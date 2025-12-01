#!/bin/bash
set -e

# Configuration
AWS_REGION="${AWS_REGION:-us-east-1}"
USERNAME="${1:-testuser}"
PASSWORD="${2:-TempPassword123!}"
EMAIL="${3:-testuser@example.com}"
PROMPT="${4:-Hi, how are you?}"
SESSION_ID="oauth-session-$(date +%s)abcdefghijklmnopqrstuvwxyz123456"

echo "🚀 OAuth2 AgentCore Invocation"
echo "👤 Username: $USERNAME"
echo "📧 Email: $EMAIL"
echo "📝 Prompt: $PROMPT"
echo ""

# Get Terraform outputs
USER_POOL_ID=$(terraform output -raw cognito_user_pool_id 2>/dev/null)
CLIENT_ID=$(terraform output -raw cognito_client_id 2>/dev/null)
AGENT_RUNTIME_ARN=$(terraform output -raw oauth_agent_runtime_arn 2>/dev/null)

if [ -z "$USER_POOL_ID" ] || [ -z "$CLIENT_ID" ] || [ -z "$AGENT_RUNTIME_ARN" ]; then
    echo "❌ Missing Terraform outputs"
    echo "💡 Deploy infrastructure first: terraform apply"
    exit 1
fi

# Step 1: Check if user exists, create if needed
echo "👤 Checking if user exists..."
USER_EXISTS=$(aws cognito-idp admin-get-user \
    --user-pool-id "$USER_POOL_ID" \
    --username "$USERNAME" \
    --region "$AWS_REGION" \
    --no-cli-pager 2>/dev/null && echo "true" || echo "false")

if [ "$USER_EXISTS" = "false" ]; then
    echo "👤 Creating new Cognito user..."
    aws cognito-idp admin-create-user \
        --user-pool-id "$USER_POOL_ID" \
        --username "$USERNAME" \
        --user-attributes Name=email,Value="$EMAIL" Name=email_verified,Value=true \
        --temporary-password "$PASSWORD" \
        --message-action SUPPRESS \
        --region "$AWS_REGION" \
        --no-cli-pager

    aws cognito-idp admin-set-user-password \
        --user-pool-id "$USER_POOL_ID" \
        --username "$USERNAME" \
        --password "$PASSWORD" \
        --permanent \
        --region "$AWS_REGION" \
        --no-cli-pager
    echo "✅ User created successfully"
else
    echo "✅ User already exists, skipping creation"
fi

# Step 2: Get OAuth2 token
echo "🔑 Getting OAuth2 token..."
RESPONSE=$(aws cognito-idp admin-initiate-auth \
    --user-pool-id "$USER_POOL_ID" \
    --client-id "$CLIENT_ID" \
    --auth-flow ADMIN_NO_SRP_AUTH \
    --auth-parameters USERNAME="$USERNAME",PASSWORD="$PASSWORD" \
    --region "$AWS_REGION" \
    --output json \
    --no-cli-pager)

ACCESS_TOKEN=$(echo "$RESPONSE" | jq -r '.AuthenticationResult.AccessToken' 2>/dev/null || echo "")

if [ "$ACCESS_TOKEN" = "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "❌ Failed to get access token"
    echo "Response: $RESPONSE"
    exit 1
fi

# Step 3: Invoke agent
echo "🚀 Invoking OAuth2 agent..."
# URL encode the ARN for the endpoint
ESCAPED_ARN=$(echo "$AGENT_RUNTIME_ARN" | sed 's/:/%3A/g' | sed 's/\//%2F/g')
PUBLIC_URL="https://bedrock-agentcore.${AWS_REGION}.amazonaws.com/runtimes/${ESCAPED_ARN}/invocations?qualifier=DEFAULT"
echo "🌐 URL: $PUBLIC_URL"

PAYLOAD='{"prompt":"'"$PROMPT"'"}'

RESPONSE=$(curl -s -X POST "$PUBLIC_URL" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $ACCESS_TOKEN" \
    -H "X-Amzn-Bedrock-AgentCore-Runtime-Session-Id: $SESSION_ID" \
    -H "X-Amzn-Bedrock-AgentCore-Runtime-User-Id: $USERNAME" \
    -H "X-Amzn-Bedrock-AgentCore-Runtime-Custom-Test: oauth-test" \
    -d "$PAYLOAD")

echo "✅ OAuth2 Agent Response:"
if command -v jq >/dev/null 2>&1 && echo "$RESPONSE" | jq . >/dev/null 2>&1; then
    echo "$RESPONSE" | jq '.'
else
    echo "$RESPONSE"
fi
