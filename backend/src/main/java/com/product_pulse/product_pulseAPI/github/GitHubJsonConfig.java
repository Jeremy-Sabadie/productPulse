package com.product_pulse.product_pulseAPI.github;

import tools.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration JSON utilisée par le client GitHub.
 */
@Configuration
public class GitHubJsonConfig {

    /**
     * Fournit le sérialiseur JSON au client HTTP GitHub.
     *
     * @return instance Jackson ObjectMapper
     */
    @Bean
    public ObjectMapper githubObjectMapper() {
        return new ObjectMapper();
    }
}
