# Simple AgentCore Agent

A minimal Spring Boot application demonstrating the AgentCore starter with background task tracking.

## Features

- **Basic AgentCore Integration**: Uses `@AgentCoreInvocation` annotation
- **Background Task Tracking**: Demonstrates `AgentCoreTaskTracker` for long-running tasks
- **AgentCore Context**: Shows how to access session headers
- **Health Monitoring**: Built-in `/ping` endpoint with HealthyBusy status

## Quick Start

The application will be available at http://localhost:8080

## What This Example Shows

The active `@AgentCoreInvocation` method demonstrates:

```java
@AgentCoreInvocation
public String manualAsyncTaskHandling(MySimpleRequest request, AgentCoreContext context) {
    agentCoreTaskTracker.increment();  // Tell runtime: background work starting
    
    CompletableFuture.runAsync(() -> {
        // 15-second background task simulation
        Thread.sleep(15000);
    }).thenRun(agentCoreTaskTracker::decrement);  // Tell runtime: work completed
    
    return "Something from Async Version";
}
```

## API Endpoints

- **Agent endpoint**: `POST /invocations` - Processes requests with background task tracking
- **Health endpoint**: `GET /ping` - Returns "HealthyBusy" during background processing

## Example Usage

```bash
# Send a request (starts 15-second background task)
curl -X POST http://localhost:8080/invocations \
  -H "Content-Type: application/json" \
  -d '{"prompt": "Hello, how are you?"}'

# Check health status (will show "HealthyBusy" for 10 seconds)
curl http://localhost:8080/ping
```

## Requirements

- Java 21
- Maven 3.6+
- Docker or Finch (optional, for `./run-local.sh docker`)
- jq (optional, for formatted JSON output)

## AWS Deployment

For AWS deployment, use the deployment scripts:
- `./deploy.sh` - Full deployment
- `./deploy.sh build` - Build container image
- `./deploy.sh push` - Push to ECR  
- `./deploy.sh agentcore` - Just AgentCore deployment
- `./deploy.sh test` - Test deployment

