package org.springaicommunity.agentcore.memory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;

// potentially auto-create memory if it doesn't exist
//   But startup will then take minutes and the application shouldn't be "ready" until the memory is created
@Configuration
@Import(AgentCoreMemoryRepositoryConfiguration.class)
public class AgentCoreMemoryRepositoryAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    BedrockAgentCoreClient bedrockAgentCoreClient() {
        return BedrockAgentCoreClient.create();
    }

    @Bean
    @ConditionalOnMissingBean
    AgentCoreMemoryRepository memoryRepository(
            AgentCoreMemoryRepositoryConfiguration configuration,
            BedrockAgentCoreClient client
            ) {
        return new AgentCoreMemoryRepository(configuration.getMemoryId(), client);
    }

}
