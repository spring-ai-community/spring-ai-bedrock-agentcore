#!/bin/bash
set -e

# Configuration
AWS_REGION="${AWS_REGION:-us-east-1}"
SESSION_ID="${1:-iam-session-$(date +%s)abcdefghijklmnopqrstuvwxyz123456}"
PROMPT="${2:-Hi, how are you?}"

echo "🔍 Finding IAM agent runtime in region: $AWS_REGION"

# Get agent runtime ARN from Terraform output
AGENT_RUNTIME_ARN=$(terraform output -raw iam_agent_runtime_arn 2>/dev/null)

if [ -z "$AGENT_RUNTIME_ARN" ]; then
    echo "❌ No IAM agent runtime found"
    echo "💡 Deploy infrastructure first: terraform apply"
    exit 1
fi

echo "📍 Found runtime ARN: $AGENT_RUNTIME_ARN"

# Validate session ID length (must be 33+ characters)
if [ ${#SESSION_ID} -lt 33 ]; then
    echo "❌ Error: Session ID must be at least 33 characters long"
    echo "Current length: ${#SESSION_ID}"
    exit 1
fi

echo "🚀 Invoking IAM agent..."
echo "📝 Prompt: $PROMPT"
echo "🔑 Session ID: $SESSION_ID"
echo ""

# Create base64 encoded payload
PAYLOAD_JSON="{\"prompt\":\"$PROMPT\"}"
PAYLOAD_B64=$(echo -n "$PAYLOAD_JSON" | base64)

# Invoke agent using AWS CLI with base64 payload
aws bedrock-agentcore invoke-agent-runtime \
    --agent-runtime-arn "$AGENT_RUNTIME_ARN" \
    --content-type "application/json" \
    --runtime-session-id "$SESSION_ID" \
    --runtime-user-id "iam-user" \
    --qualifier "DEFAULT" \
    --payload "$PAYLOAD_B64" \
    --region "$AWS_REGION" \
    --no-cli-pager \
    --output text \
    --query 'response' \
    /dev/stdout

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ IAM agent invocation completed"
else
    echo "❌ Error invoking IAM agent"
    echo "💡 Make sure:"
    echo "1. Agent runtime is deployed and active"
    echo "2. Proper AWS credentials configured"
    echo "3. IAM permissions for bedrock-agentcore:InvokeAgentRuntime"
    exit 1
fi
