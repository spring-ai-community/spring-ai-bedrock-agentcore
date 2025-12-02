# Spring AI Simple Chat Client

A lightweight Spring Boot application demonstrating Spring AI with Amazon Bedrock **without** the AgentCore starter. This shows traditional REST endpoints for comparison.

## Features

- **Traditional REST API**: Manual endpoint creation (not using `@AgentCoreInvocation`)
- **Synchronous and Streaming**: Both response types supported
- **Custom Tool Integration**: Date/time tools available to AI
- **Amazon Bedrock**: Integration with Claude 3 Sonnet model

## Comparison with AgentCore Examples

This example uses **manual REST controllers** instead of the AgentCore starter:
- Manual `@PostMapping` endpoints vs `@AgentCoreInvocation`
- Custom paths (`/ai`, `/ai/stream`) vs fixed `/invocations`
- No automatic AgentCore health monitoring

## Prerequisites

- Java 21
- Maven
- AWS account with access to Amazon Bedrock
- AWS credentials configured locally

## Configuration

Uses Amazon Bedrock's Claude 3 Sonnet model in EU West 1:

```properties
spring.application.name=agents
spring.ai.bedrock.aws.region=eu-west-1
spring.ai.bedrock.converse.chat.options.model=eu.anthropic.claude-3-7-sonnet-20250219-v1:0
```

## Building and Running

```bash
mvn spring-boot:run
```

The application starts on port 8080.

## API Endpoints

### Synchronous Chat

```bash
curl -XPOST 'http://localhost:8080/ai' \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Tell me about Spring AI in 2 sentences."}'
```

### Streaming Chat

```bash
curl -XPOST -N 'http://localhost:8080/ai/stream' \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Who is George Mallory?"}'
```

### With Tool Use

```bash
curl -XPOST -N 'http://localhost:8080/ai/stream' \
  -H "Content-Type: application/json" \
  -d '{"prompt":"What is the current date and time?"}'
```

## Project Structure

- `ChatController.java`: Manual REST endpoints (no AgentCore)
- `PromptRequest.java`: Request payload record
- `DateTimeTools.java`: Custom tool for date/time information
- `AgentsApplication.java`: Spring Boot application entry point

## Key Differences from AgentCore Examples

1. **Manual Endpoints**: Uses `@PostMapping` instead of `@AgentCoreInvocation`
2. **Custom Paths**: `/ai` and `/ai/stream` instead of `/invocations`
3. **No Health Monitoring**: No automatic `/ping` endpoint
4. **More Code**: Requires manual controller setup vs single annotation
