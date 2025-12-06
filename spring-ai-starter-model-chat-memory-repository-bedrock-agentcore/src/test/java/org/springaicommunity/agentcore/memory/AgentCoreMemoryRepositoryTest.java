package org.springaicommunity.agentcore.memory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AgentCoreMemoryRepositoryTest {

    @Mock
    private BedrockAgentCoreClient client;

    @InjectMocks
    private AgentCoreMemoryRepository memoryRepository;

    @Test
    public void createAndFetchMemories() {
        List<Message> messages = List.of(
                UserMessage.builder().text("hello").build()
        );

        CreateEventResponse response = CreateEventResponse.builder()
                        .event(Event.builder()
                                .memoryId("testMemoryId")
                                .actorId("testActorId")
                                .sessionId("testSessionId")
                                .eventId("testEventId")
                                .payload(PayloadType.builder()
                                        .conversational(Conversational.builder()
                                                .role(Role.USER)
                                                .content(Content.builder()
                                                        .text("test prompt")
                                                        .build())
                                                .build())
                                        .build())
                                .build())
                .build();
        when(client.createEvent(any(CreateEventRequest.class))).thenReturn(response);

        ListEventsResponse listEventsResponse = ListEventsResponse.builder()
                .events(Event.builder()
                        .payload(PayloadType.builder()
                                .conversational(Conversational.builder()
                                        .role(Role.USER)
                                        .content(Content.builder()
                                                .text("test prompt")
                                                .build())
                                        .build())
                                .build())
                        .build())
                        .build();
        when(client.listEvents(any(ListEventsRequest.class))).thenReturn(listEventsResponse);

        memoryRepository.saveAll("testActorId/testSessionId", messages);

        List<Message> memoryMessages = memoryRepository.findByConversationId("testActorId/testSessionId");

        assertThat(memoryMessages.size()).isEqualTo(1);
        assertThat(memoryMessages.get(0).getText()).isEqualTo("test prompt");

        ArgumentCaptor<ListEventsRequest> requestArgumentCaptor = ArgumentCaptor.forClass(ListEventsRequest.class);
        verify(client).listEvents(requestArgumentCaptor.capture());
        assertThat(requestArgumentCaptor.getValue().actorId()).isEqualTo("testActorId");
        assertThat(requestArgumentCaptor.getValue().sessionId()).isEqualTo("testSessionId");
        assertThat(requestArgumentCaptor.getValue().memoryId()).isEqualTo("chat-memory");
    }

}
