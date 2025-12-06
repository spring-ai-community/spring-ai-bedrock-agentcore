package org.springaicommunity.agentcore.memory;

import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.*;

import java.time.Instant;
import java.util.List;

public class AgentCoreMemoryRepository implements ChatMemoryRepository {

    private final BedrockAgentCoreClient client;

    private final String memoryId;

    public AgentCoreMemoryRepository(String memoryId, BedrockAgentCoreClient client) {
        this.memoryId = memoryId;
        this.client = client;
    }

    record ActorAndSession(String actor, String session) { }

    static ActorAndSession actorAndSession(String conversationId) {
        // todo: validation
        var parts = conversationId.split("/");
        return new ActorAndSession(parts[0], parts[1]);
    }

    static String conversationId(String actor, String session) {
        return actor + "/" + session;
    }

    @Override
    public List<String> findConversationIds() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        var actorAndSession = actorAndSession(conversationId);

        var listEventsRequest = ListEventsRequest.builder()
                .actorId(actorAndSession.actor())
                .sessionId(actorAndSession.session())
                .memoryId(memoryId) // todo: from config
                .includePayloads(true)
                .maxResults(100) // todo: not sure how to manage this other than config but if a window size is larger than the config, messages will be lost
                .build();

        var listEventsResponse = client.listEvents(listEventsRequest);

        return listEventsResponse.events().stream().flatMap(event ->
                event.payload().stream().map(payload ->
                        switch (payload.conversational().role()) {
                            case ASSISTANT -> new AssistantMessage(payload.conversational().content().text());
                            case USER -> new UserMessage(payload.conversational().content().text());
                            // todo: handle other roles & potentially configurable
                            default -> throw new IllegalStateException("Unexpected value: " + payload.conversational().role());
                        }
                )
        ).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        var actorAndSession = actorAndSession(conversationId);

        var payloads = messages.stream().map(message -> {
                    Role role;

                    if (message instanceof AssistantMessage) {
                        role = Role.ASSISTANT;
                    } else if (message instanceof UserMessage) {
                        role = Role.USER;
                    } else {
                        throw new IllegalStateException("Unexpected value: " + message);
                    }

                    var content = Content.builder().text(message.getText()).build();

                    var conversational = Conversational.builder().content(content).role(role).build();

                    return PayloadType.builder().conversational(conversational).build();
                }
        ).toList();

        var createEventRequest = CreateEventRequest.builder()
                .memoryId(memoryId) // todo: to config
                .actorId(actorAndSession.actor())
                .sessionId(actorAndSession.session())
                .payload(payloads)
                .eventTimestamp(Instant.now())
                .build();

        client.createEvent(createEventRequest);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        var actorAndSession = actorAndSession(conversationId);

        // todo: handle pagination
        var listEventsRequest = ListEventsRequest.builder()
                .memoryId(memoryId) // todo: to config
                .actorId(actorAndSession.actor())
                .sessionId(actorAndSession.session())
                .includePayloads(false)
                .maxResults(100) // todo: to config?
                .build();

        var events = client.listEvents(listEventsRequest).events();

        events.forEach(event -> client.deleteEvent(
                    DeleteEventRequest.builder()
                            .memoryId(memoryId)
                            .actorId(actorAndSession.actor())
                            .sessionId(actorAndSession.session())
                            .eventId(event.eventId())
                            .build()
                )
        );
    }
}
