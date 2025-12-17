package org.springaicommunity.agentcore.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;

@Configuration
@EnableConfigurationProperties(AgentCoreShortMemoryRepositoryConfiguration.class)
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
		return new AgentCoreShortMemoryRepository(configuration.memoryId(), client, configuration.totalEventsLimit(),
				configuration.defaultSession(), configuration.pageSize(), configuration.ignoreUnknownRoles());
	}

}
