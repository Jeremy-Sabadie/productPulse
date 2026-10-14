
package com.product_pulse.product_pulseAPI.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité de l'API ProductPulse.
 *
 * <p>L'API est sans session et applique une politique
 * de refus par défaut.</p>
 *
 * <p>L'authentification JWT sera activée uniquement
 * après configuration d'un décodeur fiable.</p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Définit les règles de protection HTTP.
     *
     * @param http configuration de sécurité Spring
     * @return chaîne des filtres de sécurité
     * @throws Exception si la configuration échoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/feedback"
                        ).denyAll()
                        .anyRequest().denyAll());

        return http.build();
    }
}
