package org.springaicommunity.agentcore.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "agentcore.memory")
public record AgentCoreShortMemoryRepositoryConfiguration(String memoryId, Integer totalEventsLimit,
		String defaultSession, int pageSize, boolean ignoreUnknownRoles) {

	public AgentCoreShortMemoryRepositoryConfiguration(String memoryId, Integer totalEventsLimit, String defaultSession,
			int pageSize, boolean ignoreUnknownRoles) {
		this.memoryId = memoryId;
		this.totalEventsLimit = totalEventsLimit;
		this.defaultSession = defaultSession != null ? defaultSession : "default-session";
		this.pageSize = pageSize > 0 ? pageSize : 100;
		this.ignoreUnknownRoles = ignoreUnknownRoles;
	}

}
