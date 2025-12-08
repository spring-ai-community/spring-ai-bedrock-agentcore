package com.unicorn.agents;

import org.springaicommunity.agentcore.controller.AgentCoreInvocationsHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class CustomAgentCoreInvocationsController implements AgentCoreInvocationsHandler {

    private final ChatClient chatClient;

    public CustomAgentCoreInvocationsController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient
                .defaultTools(new DateTimeTools())
                .build();
    }

    @PostMapping(value = "/invocations", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> handleJsonInvocation(@RequestBody Object request, @RequestHeader HttpHeaders headers) {
        return chatClient.prompt().user((String) request).stream().content();
    }
}
