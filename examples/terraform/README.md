# Combined Terraform AgentCore Deployment

Single Terraform stack that deploys both IAM and OAuth2 authenticated AgentCore runtimes.

## Setup

1. **Build and push image:**
   ```bash
   ./build-and-push.sh
   ```

2. **Deploy infrastructure:**
   ```bash
   terraform init
   terraform apply
   ```

## Testing

### IAM Authentication
```bash
# Basic test
./invoke-iam.sh

# Custom session and prompt
./invoke-iam.sh "my-session-id-123456789012345678901" "What is Spring AI?"
```

### OAuth2 Authentication
```bash
# Basic test (creates user if needed)
./invoke-oauth.sh

# Custom user and prompt
./invoke-oauth.sh myuser MyPassword123! user@example.com "What is Spring AI?"
```

## What Gets Deployed

### Shared Resources
- ECR repository for container images
- Random domain suffix for Cognito

### IAM Stack
- `SpringAiAgentJavaIam` - AgentCore runtime with IAM authentication
- IAM role with Bedrock permissions
- Custom header support

### OAuth2 Stack  
- `SpringAiAgentJavaOauth` - AgentCore runtime with OAuth2 authentication
- Cognito User Pool and Client
- JWT authorization with custom headers
- Public HTTPS endpoint access

## Configuration

Edit `terraform.tfvars`:
```hcl
region   = "us-east-1"
app_name = "SpringAiAgentJava"
```

## Available Examples

- `simple-spring-boot-app` - Basic AgentCore integration
- `spring-ai-sse-chat-client` - SSE streaming with Spring AI
- `spring-ai-simple-chat-client` - Simple Spring AI integration

## Workflow

1. **Build**: `./build-and-push.sh` - Creates ECR and pushes image
2. **Deploy**: `terraform apply` - Creates both runtimes
3. **Test IAM**: `./invoke-iam.sh` - Uses AWS credentials
4. **Test OAuth2**: `./invoke-oauth.sh` - Uses Cognito authentication
