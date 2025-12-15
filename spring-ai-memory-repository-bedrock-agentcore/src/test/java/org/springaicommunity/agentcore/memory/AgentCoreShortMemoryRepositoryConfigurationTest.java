package org.springaicommunity.agentcore.memory;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AgentCoreShortMemoryRepositoryConfigurationTest {

	@Test
	void shouldHaveDefaultValues() {
		var config = new AgentCoreShortMemoryRepositoryConfiguration();

		assertThat(config.getDefaultSession()).isEqualTo("default-session");
		assertThat(config.getPageSize()).isEqualTo(100);
		assertThat(config.isIgnoreUnknownRoles()).isFalse();
		assertThat(config.getTotalEventsLimit()).isNull();
		assertThat(config.getMemoryId()).isNull();
	}

	@Test
	void shouldSetAndGetAllProperties() {
		var config = new AgentCoreShortMemoryRepositoryConfiguration();

		config.setMemoryId("test-memory-id");
		config.setTotalEventsLimit(500);
		config.setDefaultSession("custom-session");
		config.setPageSize(50);
		config.setIgnoreUnknownRoles(true);

		assertThat(config.getMemoryId()).isEqualTo("test-memory-id");
		assertThat(config.getTotalEventsLimit()).isEqualTo(500);
		assertThat(config.getDefaultSession()).isEqualTo("custom-session");
		assertThat(config.getPageSize()).isEqualTo(50);
		assertThat(config.isIgnoreUnknownRoles()).isTrue();
	}

}
