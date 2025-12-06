/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springaicommunity.agentcore.memory;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.CreateMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.DeleteMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.GetMemoryRequest;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.MemoryStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = EndToEndIntegrationTest.TestApp.class,
        properties = {
            "agentcore.memory.memory-id=test_memory_1-SYnXWsA0Sj",
//            "spring.ai.bedrock.converse.chat.enabled=true",
            "spring.ai.bedrock.converse.chat.options.model=global.amazon.nova-2-lite-v1:0"
        })
class EndToEndIntegrationTest {

    /*
    static String memoryId;

    static BedrockAgentCoreControlClient client = BedrockAgentCoreControlClient.create();


     */
	@SpringBootApplication(scanBasePackages = "org.springaicommunity.agentcore.memory")
	static class TestApp {

        /*
		public static class TestAgent {
            private final AgentCoreMemoryRepository memoryRepository;

            public TestAgent(AgentCoreMemoryRepository memoryRepository) {
                this.memoryRepository = memoryRepository;
            }
        }

         */

	}

	@Autowired
	private AgentCoreMemoryRepository chatMemoryRepository;

    @Autowired
    private ChatModel chatModel;

    /*
    // todo: de-dupe
    @BeforeAll
    public static void setup() throws InterruptedException {
        var createMemoryRequest = CreateMemoryRequest.builder()
                .name("test_memory_2")
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

     */

    @Test
	void shouldHandleStringRequest() {

        var chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        var conversationId = "testActorId/testSessionId";

        chatMemory.clear(conversationId);

        /*
        List<Message> messages = List.of(
                UserMessage.builder().text("my name is James").build()
        );

        chatMemory.add(conversationId, messages);
         */

        var chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();

        chatClient.prompt()
                .user("My name is James")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        var response = chatClient.prompt()
                .user("What is my name?")
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        System.out.println(response);

        assertThat(response).containsIgnoringCase("James");
	}

}
