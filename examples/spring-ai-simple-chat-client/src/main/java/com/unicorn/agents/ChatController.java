package com.unicorn.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springaicommunity.agentcore.context.AgentCoreHeaders;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {

	private final ChatClient chatClient;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

	public ChatController (ChatClient.Builder chatClient){
		this.chatClient = chatClient
				.defaultTools(new DateTimeTools())
				.build();
	}

    @AgentCoreInvocation
    public String agentCoreHandler(PromptRequest promptRequest, AgentCoreContext agentCoreContext){
        logger.info(agentCoreContext.getHeader(AgentCoreHeaders.SESSION_ID));
        return chatClient.prompt().user(promptRequest.prompt()).call().content();
    }
}
