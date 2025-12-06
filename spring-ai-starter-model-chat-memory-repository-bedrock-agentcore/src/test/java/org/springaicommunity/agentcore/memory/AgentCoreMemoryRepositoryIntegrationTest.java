package org.springaicommunity.agentcore.memory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.*;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.DeleteMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.GetMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.MemoryStatus;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AgentCoreMemoryRepositoryIntegrationTest {

    static String memoryId;

    static BedrockAgentCoreControlClient client = BedrockAgentCoreControlClient.create();


    @BeforeAll
    public static void setup() throws InterruptedException {
        var createMemoryRequest = CreateMemoryRequest.builder()
                .name("test_memory_1")
                .eventExpiryDuration(100) // todo: file bug for this taking an integer
                .build();
        var createMemoryResponse = client.createMemory(createMemoryRequest);

        memoryId = createMemoryResponse.memory().id();

        var memoryCreated = false;

        // todo: timeout potentially with www.awaitility.org
        while (!memoryCreated) {
            System.out.println("Waiting for memory to be ACTIVE...");
            var getMemoryRequest = GetMemoryRequest.builder().memoryId(memoryId).build();
            var getMemoryResponse = client.getMemory(getMemoryRequest);
            memoryCreated = getMemoryResponse.memory().status() == MemoryStatus.ACTIVE;
            Thread.sleep(3000);
        }
    }

    @AfterAll
    public static void teardown() {
        System.out.println("Deleting memory: " + memoryId);
        var deleteMemoryRequest = DeleteMemoryRequest.builder().memoryId(memoryId).build();
        var deleteMemoryResponse = client.deleteMemory(deleteMemoryRequest);
        System.out.println("Deleted memory: " + deleteMemoryResponse.statusAsString());
    }

    @Test
    public void createAndFetchMemories() {
        var bedrockAgentCoreClient = BedrockAgentCoreClient.create();
        var agentCoreMemoryRepository = new AgentCoreMemoryRepository(memoryId, bedrockAgentCoreClient);
        List<Message> messages = List.of(
                UserMessage.builder().text("hello").build()
        );

        agentCoreMemoryRepository.saveAll("testActorId/testSessionId", messages);

        List<Message> fetchedEvents = agentCoreMemoryRepository.findByConversationId("testActorId/testSessionId");

        assertThat(fetchedEvents.size()).isEqualTo(1);
    }

    @Test
    public void testChatMemory() {
        var bedrockAgentCoreClient = BedrockAgentCoreClient.create();
        var agentCoreMemoryRepository = new AgentCoreMemoryRepository(memoryId, bedrockAgentCoreClient);

        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(agentCoreMemoryRepository)
                .maxMessages(10)
                .build();

        var messages = new ArrayList<Message>();
        for (int i = 0; i < 20; i++) {
            messages.add(UserMessage.builder().text("test message " + i).build());
        }

        chatMemory.add("user1/session1", messages);

        var memories = chatMemory.get("user1/session1");
        assertThat(memories.size()).isEqualTo(10);
    }

}
