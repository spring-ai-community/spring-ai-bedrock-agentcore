# Spring AI AgentCore Memory Example

A complete example demonstrating how to integrate **Spring AI** with **AWS Bedrock AgentCore Memory** for persistent, context-aware conversations. This example includes both the application code and Terraform infrastructure for a production-ready setup.

## 🎯 What This Example Demonstrates

- **Persistent Conversations**: Chat history stored in AWS Bedrock AgentCore Memory
- **Context Awareness**: AI remembers previous conversation context
- **RESTful API**: Clean REST endpoints for chat interactions
- **Infrastructure as Code**: Terraform setup for AgentCore Memory
- **Production Ready**: Error handling, logging, and configuration management
- **Spring AI Integration**: Seamless integration with Spring AI framework

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────────┐
│   REST Client   │───▶│  ChatController  │───▶│   Spring AI Chat   │
│   (curl/web)    │    │                  │    │      Client         │
└─────────────────┘    └──────────────────┘    └─────────────────────┘
                                ▲                          │
                                │                          ▼
                                │               ┌─────────────────────┐
                                │               │  Bedrock Converse   │
                                │               │       API           │
                                │               └─────────────────────┘
                                │
                       ┌──────────────────┐
                       │   ChatMemory     │
                       │  (Window-based)  │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ AgentCore Memory │
                       │   Repository     │
                       └──────────────────┘
                                │
                                ▼
                       ┌──────────────────┐
                       │ AWS Bedrock      │
                       │ AgentCore Memory │
                       └──────────────────┘
```

## 🚀 Quick Start

### Option 1: Automated Deployment

```bash
# Clone and navigate to example
cd examples/spring-ai-agent-core-memory

# Deploy infrastructure and build application
./deploy.sh

# Run the application (memory ID will be set automatically)
java -jar target/spring-ai-agent-core-memory-1.0.0-SNAPSHOT.jar
```

### Option 2: Manual Setup

#### 1. Prerequisites

- **Java 17+**
- **Maven 3.6+**
- **AWS CLI** configured with appropriate permissions
- **Terraform 1.0+**
- **jq** (for JSON parsing)

#### 2. Create Infrastructure

```bash
cd terraform
terraform init
terraform plan
terraform apply

# Get the memory ID
export AGENTCORE_MEMORY_ID=$(terraform output -raw memory_id)
```

**Note**: The Terraform configuration uses AWS CLI commands via `null_resource` because the Terraform AWS provider doesn't yet support the `aws_bedrockagentcore_memory` resource type.

#### 3. Build and Run

```bash
mvn clean package
java -jar target/spring-ai-agent-core-memory-1.0.0-SNAPSHOT.jar
```

## 📡 API Reference

### Chat Endpoints

#### Send Message
```http
POST /api/chat/{conversationId}
Content-Type: application/json

{
  "message": "Hello, my name is Alice"
}
```

**Response:**
```json
{
  "response": "Hello Alice! Nice to meet you. How can I help you today?"
}
```

#### Get Conversation History
```http
GET /api/chat/{conversationId}/history
```

**Response:**
```json
[
  {
    "messageType": "USER",
    "textContent": "Hello, my name is Alice"
  },
  {
    "messageType": "ASSISTANT", 
    "textContent": "Hello Alice! Nice to meet you. How can I help you today?"
  }
]
```

#### Clear Conversation
```http
DELETE /api/chat/{conversationId}
```

## 💬 Example Conversation Flow

### 1. Start Conversation

```bash
curl -X POST http://localhost:8080/api/chat/alice \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello, my name is Alice and I love hiking"}'
```

**Response:**
```json
{
  "response": "Hello Alice! It's great to meet you. I'd love to hear more about your hiking adventures! Do you have any favorite trails or destinations?"
}
```

### 2. Continue with Context

```bash
curl -X POST http://localhost:8080/api/chat/alice \
  -H "Content-Type: application/json" \
  -d '{"message": "What do you remember about me?"}'
```

**Response:**
```json
{
  "response": "I remember that your name is Alice and you mentioned that you love hiking! Are there any specific hiking topics you'd like to discuss?"
}
```

### 3. Check History

```bash
curl http://localhost:8080/api/chat/alice/history
```

### 4. Multiple Conversations

```bash
# Different conversation - separate context
curl -X POST http://localhost:8080/api/chat/bob \
  -H "Content-Type: application/json" \
  -d '{"message": "Hi, I am Bob"}'
```

## ⚙️ Configuration

### Application Configuration (`application.yml`)

```yaml
# Spring AI Configuration
spring:
  ai:
    bedrock:
      converse:
        chat:
          model: anthropic.claude-3-5-sonnet-20241022-v2:0
          options:
            temperature: 0.7
            max-tokens: 1000

# AgentCore Memory Configuration
agentcore:
  memory:
    memory-id: ${AGENTCORE_MEMORY_ID}
    total-events-limit: 100        # Limit conversation history
    default-session: default       # Default session name
    page-size: 50                  # API pagination size
    ignore-unknown-roles: true     # Handle unknown message types gracefully

# Server Configuration
server:
  port: 8080

# Logging Configuration
logging:
  level:
    org.springaicommunity.agentcore.memory: DEBUG
    org.springframework.ai: INFO
```

### Terraform Configuration (`terraform/variables.tf`)

```hcl
variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
}

variable "memory_name" {
  description = "Name for the AgentCore Memory"
  type        = string
  default     = "spring-ai-example-memory"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "development"
}
```

## 🔧 Customization

### Memory Window Configuration

```java
@Configuration
public class ChatConfig {
    
    @Bean
    public ChatMemory chatMemory(ChatMemoryRepository memoryRepository) {
        return MessageWindowChatMemory.builder()
            .chatMemoryRepository(memoryRepository)
            .maxMessages(20)  // Keep last 20 messages in context
            .build();
    }
}
```

### Custom Chat Logic

```java
@RestController
public class CustomChatController {
    
    @PostMapping("/api/chat/{conversationId}/summarize")
    public SummaryResponse summarizeConversation(@PathVariable String conversationId) {
        List<Message> history = chatMemory.get(conversationId, 50);
        
        String summary = chatClient.prompt()
            .user("Summarize this conversation: " + formatHistory(history))
            .call()
            .content();
            
        return new SummaryResponse(summary);
    }
}
```

### Environment-Specific Configuration

```yaml
# application-dev.yml
agentcore:
  memory:
    total-events-limit: 50
    ignore-unknown-roles: true

# application-prod.yml  
agentcore:
  memory:
    total-events-limit: 200
    ignore-unknown-roles: false
```

## 🏗️ Infrastructure Details

### Terraform Resources

The example creates AgentCore Memory using a `null_resource` with AWS CLI commands because the Terraform AWS provider doesn't yet support the `aws_bedrockagentcore_memory` resource type.

**What gets created:**
1. **AgentCore Memory**: Using `aws bedrock-agentcore-control create-memory`
2. **JSON Output File**: Stores memory details for Terraform state
3. **Automatic Cleanup**: Destroys memory on `terraform destroy`

**Why this approach:**
- **Bedrock AgentCore** is a new AWS service
- **Terraform AWS Provider** hasn't added support yet
- **AWS CLI** provides full functionality
- **Terraform manages** the lifecycle properly

### Resource Outputs

```bash
# Get all outputs
terraform output

# Specific outputs
terraform output memory_id
terraform output memory_arn
terraform output export_command
```

### Infrastructure Cleanup

```bash
cd terraform
terraform destroy
```

## 🔍 Monitoring and Debugging

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Debug Logging

Enable detailed logging in `application.yml`:

```yaml
logging:
  level:
    org.springaicommunity.agentcore.memory: DEBUG
    software.amazon.awssdk.services.bedrockagentcore: DEBUG
    org.springframework.ai: DEBUG
```

### Memory Usage Monitoring

```bash
# Check conversation size
curl http://localhost:8080/api/chat/alice/history | jq length

# Monitor application logs
tail -f logs/application.log | grep "AgentCore"
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Memory ID Not Found
```
Error: AgentCore Memory not found
Solution: Verify AGENTCORE_MEMORY_ID environment variable
```

```bash
# Check if variable is set
echo $AGENTCORE_MEMORY_ID

# Set manually if needed
export AGENTCORE_MEMORY_ID=your-memory-id-here
```

#### 2. AWS Permissions
```
Error: Access denied to AgentCore Memory
Solution: Check IAM permissions
```

Required permissions:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock-agentcore:ListEvents",
        "bedrock-agentcore:CreateEvent", 
        "bedrock-agentcore:DeleteEvent"
      ],
      "Resource": "*"
    }
  ]
}
```

#### 3. Region Mismatch
```
Error: Memory not found in region
Solution: Ensure consistent AWS region
```

```bash
# Check AWS CLI region
aws configure get region

# Update Terraform region
cd terraform
terraform apply -var="aws_region=us-west-2"
```

#### 4. Model Access
```
Error: Model access denied
Solution: Enable model access in Bedrock console
```

1. Go to AWS Bedrock Console
2. Navigate to "Model access"
3. Enable "Anthropic Claude 3.5 Sonnet"

#### 5. Terraform Resource Not Supported
```
Error: The provider hashicorp/aws does not support resource type "aws_bedrockagentcore_memory"
Solution: This is expected - we use AWS CLI via null_resource
```

The current Terraform configuration uses `null_resource` with AWS CLI commands because:
- Bedrock AgentCore is a new service
- Terraform AWS provider doesn't support it yet
- This approach works reliably with the AWS CLI

#### 6. Missing jq Command
```
Error: jq: command not found
Solution: Install jq for JSON parsing
```

```bash
# macOS
brew install jq

# Ubuntu/Debian
sudo apt-get install jq

# Amazon Linux
sudo yum install jq
```

### Debug Commands

```bash
# Test AWS connectivity
aws bedrock-agentcore list-memories

# Test application connectivity
curl -v http://localhost:8080/api/chat/test \
  -H "Content-Type: application/json" \
  -d '{"message": "test"}'

# Check application logs
docker logs spring-ai-app 2>&1 | grep ERROR
```

## 📊 Performance Considerations

### Memory Optimization

```yaml
agentcore:
  memory:
    total-events-limit: 100  # Prevent large memory usage
    page-size: 50           # Optimize API calls
```

### Conversation Management

```java
// Implement conversation cleanup
@Scheduled(fixedRate = 3600000) // Every hour
public void cleanupOldConversations() {
    // Archive conversations older than 30 days
    conversationService.archiveOldConversations(Duration.ofDays(30));
}
```

## 🔐 Security Best Practices

### Environment Variables

```bash
# Use environment-specific configurations
export AGENTCORE_MEMORY_ID=prod-memory-id
export AWS_REGION=us-east-1
export SPRING_PROFILES_ACTIVE=production
```

### IAM Least Privilege

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "bedrock-agentcore:ListEvents",
        "bedrock-agentcore:CreateEvent",
        "bedrock-agentcore:DeleteEvent"
      ],
      "Resource": "arn:aws:bedrock-agentcore:us-east-1:123456789012:memory/specific-memory-id"
    }
  ]
}
```

## 📈 Scaling Considerations

### Horizontal Scaling

- **Stateless Design**: Application can be scaled horizontally
- **Shared Memory**: Multiple instances share the same AgentCore Memory
- **Load Balancing**: Use conversation ID for session affinity if needed

### Performance Tuning

```yaml
# High-throughput configuration
agentcore:
  memory:
    page-size: 100
    total-events-limit: 50

spring:
  ai:
    bedrock:
      converse:
        chat:
          options:
            max-tokens: 500  # Reduce for faster responses
```

## 🧪 Testing

### Unit Tests

```bash
mvn test
```

### Integration Tests

```bash
# Start application
java -jar target/spring-ai-agent-core-memory-1.0.0-SNAPSHOT.jar &

# Run integration tests
./test-integration.sh
```

### Load Testing

```bash
# Simple load test
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/chat/user$i \
    -H "Content-Type: application/json" \
    -d '{"message": "Hello"}' &
done
```

## 📚 Additional Resources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [AWS Bedrock AgentCore Documentation](https://docs.aws.amazon.com/bedrock-agentcore/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request

## 📄 License

This project is licensed under the Apache License 2.0.
