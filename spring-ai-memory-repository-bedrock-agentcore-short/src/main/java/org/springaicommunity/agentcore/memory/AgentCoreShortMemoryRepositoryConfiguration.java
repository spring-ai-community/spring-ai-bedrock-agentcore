package org.springaicommunity.agentcore.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agentcore.memory")
public class AgentCoreShortMemoryRepositoryConfiguration {

	private String memoryId;

	public void setMemoryId(String memoryId) {
		this.memoryId = memoryId;
	}

	public String getMemoryId() {
		return memoryId;
	}

}
