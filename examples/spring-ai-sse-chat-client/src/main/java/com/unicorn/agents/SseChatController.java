package com.unicorn.agents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springaicommunity.agentcore.context.AgentCoreContext;
import org.springaicommunity.agentcore.context.AgentCoreHeaders;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@RestController
public class SseChatController {

	private final ChatClient chatClient;
    private static final Logger logger = LoggerFactory.getLogger(SseChatController.class);

	public SseChatController(ChatClient.Builder chatClient) {
		this.chatClient = chatClient
				.defaultTools(new DateTimeTools())
				.build();
	}

//	@AgentCoreInvocation
	public Flux<String> streamingAgent(String prompt) {
		return chatClient.prompt().user(prompt).stream().content();
	}

	@AgentCoreInvocation
	public Flux<String> asyncStreamingAgent(TestRequest request) {
		return chatClient.prompt().user(request.prompt).stream().content()
				.flatMapSequential(chunk -> Flux.just(chunk)
						.subscribeOn(Schedulers.parallel())
						// Converting chunks to upper case in parallel threads
						.map(c -> {
							System.out.println("Processing chunk '" + c + "' on thread: " + Thread.currentThread().getName());
							return c.toUpperCase();
						}));
	}

    //@AgentCoreInvocation
    public String synchronousAgent(PromptRequest promptRequest, AgentCoreContext agentCoreContext){
        logger.info(agentCoreContext.getHeader(AgentCoreHeaders.SESSION_ID));
        return chatClient.prompt().user(promptRequest.prompt()).call().content();
    }

    public record TestRequest(String prompt){}
}