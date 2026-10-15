
package com.product_pulse.product_pulseAPI.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Vérifie les JWT RS256 émis par le fournisseur d'identité.
 *
 * La clé publique permet uniquement de vérifier les signatures.
 */
@Configuration
public class JwtDecoderConfig {

    /**
     * Configure la validation cryptographique et métier des JWT.
     *
     * @param publicKeyPem clé publique RSA au format PEM
     * @param issuer émetteur autorisé
     * @param audience destinataire autorisé
     * @return décodeur JWT sécurisé
     */
    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.public-key}") String publicKeyPem,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience
    ) throws Exception {

        Objects.requireNonNull(publicKeyPem);
        Objects.requireNonNull(issuer);
        Objects.requireNonNull(audience);

        if (publicKeyPem.isBlank() || issuer.isBlank() || audience.isBlank()) {
            throw new IllegalArgumentException("JWT configuration is incomplete");
        }

        String encodedKey = publicKeyPem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        byte[] keyBytes = Base64.getDecoder().decode(encodedKey);

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory
                .getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(keyBytes));

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(publicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                                new OAuth2Error(
                                        "invalid_token",
                                        "Invalid token audience",
                                        null
                                )
                        );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        List.of(
                                JwtValidators.createDefaultWithIssuer(issuer),
                                audienceValidator
                        )
                )
        );

        return decoder;
    }
}
