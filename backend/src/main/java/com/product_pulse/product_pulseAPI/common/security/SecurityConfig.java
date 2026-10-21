
package com.product_pulse.product_pulseAPI.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Sécurise l'API ProductPulse avec des JWT RS256.
 *
 * Les administrateurs sont identifiés par une liste
 * d'identifiants configurée exclusivement côté serveur.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configure les autorisations HTTP.
     *
     * @param http configuration de sécurité
     * @param jwtAuthenticationConverter conversion JWT vers authentification
     * @return chaîne des filtres HTTP
     * @throws Exception si la configuration échoue
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {

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
                        resourceServer.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/feedback"
                        ).authenticated()
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/admin/feedback"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/admin/feedback/*/approve",
                                "/api/admin/feedback/*/reject"
                        ).hasRole("ADMIN")
                        .anyRequest().denyAll()
                );

        return http.build();
    }

    /**
     * Attribue ROLE_ADMIN uniquement aux utilisateurs
     * autorisés dans la configuration de ProductPulse.
     *
     * Aucun rôle fourni par le JWT n'est accepté.
     *
     * @param adminUserIds identifiants autorisés, séparés par des virgules
     * @return convertisseur d'authentification JWT
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            @Value("${app.admin.user-ids:}") String adminUserIds
    ) {
        Set<String> administrators = Arrays.stream(
                        adminUserIds.split(",")
                )
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toUnmodifiableSet());

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt ->
                administrators.contains(jwt.getSubject())
                        ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                        : List.of()
        );

        return converter;
    }
}
