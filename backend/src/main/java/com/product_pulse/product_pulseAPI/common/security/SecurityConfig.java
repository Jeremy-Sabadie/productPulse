
package com.product_pulse.product_pulseAPI.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la sécurité HTTP de ProductPulse.
 *
 * L'API utilise exclusivement des JWT Bearer vérifiés par
 * le décodeur RS256. Aucune session HTTP n'est créée.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Définit les règles d'accès aux endpoints.
     *
     * @param http configuration HTTP Spring Security
     * @return chaîne de filtres de sécurité
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                .oauth2ResourceServer(resourceServer ->
                        resourceServer.jwt(Customizer.withDefaults())
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/feedback"
                        ).authenticated()
                        .anyRequest().denyAll()
                );

        return http.build();
    }
}
