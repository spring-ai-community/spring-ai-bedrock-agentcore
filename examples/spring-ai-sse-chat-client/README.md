# Spring AI SSE Chat Client

A Spring Boot application demonstrating Server-Sent Events (SSE) streaming with Spring AI and Amazon Bedrock using the AgentCore starter.

## Features

- **SSE Streaming**: Real-time streaming responses using Server-Sent Events
- **AgentCore Integration**: Uses `@AgentCoreInvocation` for automatic endpoint creation
- **Spring AI Integration**: Seamless integration with ChatClient and tools
- **Reactive Streaming**: Returns `Flux<String>` for streaming responses
- **Tool Support**: Custom tool integration with date/time functionality
- **Amazon Bedrock**: Integration with Claude 3 Sonnet model

## What This Example Shows

```java
@AgentCoreInvocation
public Flux<String> asyncStreamingAgent(TestRequest request) {
    return chatClient.prompt().user(request.prompt).stream().content()
            .flatMapSequential(chunk -> Flux.just(chunk)
                    .subscribeOn(Schedulers.parallel())
                    .map(c -> c.toUpperCase()));
}
```

## Prerequisites

- Java 21
- Maven
- AWS account with access to Amazon Bedrock
- AWS credentials configured locally

## Configuration

The application uses Amazon Bedrock's Claude 3 Sonnet model in EU West 1:

```properties
spring.application.name=sse-agents
spring.ai.bedrock.aws.region=eu-west-1
spring.ai.bedrock.converse.chat.options.model=eu.anthropic.claude-3-7-sonnet-20250219-v1:0
```

## Building and Running

```bash
mvn spring-boot:run
```

The application starts on port 8080.

## API Usage

### SSE Streaming Endpoint

The `@AgentCoreInvocation` annotation automatically creates the `/invocations` endpoint:

```bash
curl -XPOST -N 'http://localhost:8080/invocations' \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"prompt":"Tell me about Spring AI in detail."}'
```

### With Tool Use

```bash
curl -XPOST -N 'http://localhost:8080/invocations' \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"prompt":"What is the current date and time?"}'
```

## SSE Response Format

Streaming responses follow Server-Sent Events format:

```
data: HELLO
data: ,
data:  THIS
data:  IS
data:  A
data:  STREAMING
data:  RESPONSE
data: .
```

## Project Structure

- `SseChatController.java`: Controller with `@AgentCoreInvocation` for SSE streaming
- `DateTimeTools.java`: Custom tool for date/time information
- `SseAgentsApplication.java`: Spring Boot application entry point

## Key Features

1. **SSE Streaming**: Automatic Server-Sent Events format with `Flux<String>`
2. **AgentCore Integration**: Single annotation creates the endpoint
3. **Reactive Processing**: Parallel chunk processing with uppercase transformation
4. **Tool Integration**: Custom tools available to the AI model