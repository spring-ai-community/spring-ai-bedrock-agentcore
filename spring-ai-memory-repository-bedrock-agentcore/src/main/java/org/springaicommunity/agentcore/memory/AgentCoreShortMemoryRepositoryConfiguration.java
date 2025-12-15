package org.springaicommunity.agentcore.memory;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "agentcore.memory")
public class AgentCoreShortMemoryRepositoryConfiguration {

	private String memoryId;

	private Integer totalEventsLimit;

	private String defaultSession = "default-session";

	private int pageSize = 100;

	private boolean ignoreUnknownRoles = false;

	public void setMemoryId(String memoryId) {
		this.memoryId = memoryId;
	}

	public String getMemoryId() {
		return memoryId;
	}

	public void setTotalEventsLimit(Integer totalEventsLimit) {
		this.totalEventsLimit = totalEventsLimit;
	}

	public Integer getTotalEventsLimit() {
		return totalEventsLimit;
	}

	public void setDefaultSession(String defaultSession) {
		this.defaultSession = defaultSession;
	}

	public String getDefaultSession() {
		return defaultSession;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setIgnoreUnknownRoles(boolean ignoreUnknownRoles) {
		this.ignoreUnknownRoles = ignoreUnknownRoles;
	}

	public boolean isIgnoreUnknownRoles() {
		return ignoreUnknownRoles;
	}

}
