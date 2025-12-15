package org.springaicommunity.agentcore.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;

@Configuration
@Import(AgentCoreShortMemoryRepositoryConfiguration.class)
public class AgentCoreShortMemoryRepositoryAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	BedrockAgentCoreClient bedrockAgentCoreClient() {
		return BedrockAgentCoreClient.create();
	}

	@Bean
	@ConditionalOnMissingBean
	AgentCoreShortMemoryRepository memoryRepository(AgentCoreShortMemoryRepositoryConfiguration configuration,
			BedrockAgentCoreClient client) {
		return new AgentCoreShortMemoryRepository(configuration.getMemoryId(), client);
	}

}
