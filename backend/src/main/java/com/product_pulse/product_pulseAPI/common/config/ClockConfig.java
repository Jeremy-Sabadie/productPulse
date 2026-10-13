
package com.product_pulse.product_pulseAPI.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Configuration des dépendances liées à la gestion du temps.
 */
@Configuration
public class ClockConfig {

    /**
     * Fournit une horloge système UTC aux services métier.
     *
     * @return horloge système UTC
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
