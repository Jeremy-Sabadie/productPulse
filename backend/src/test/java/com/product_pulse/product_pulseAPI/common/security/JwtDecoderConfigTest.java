
package com.product_pulse.product_pulseAPI.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires du décodeur JWT RS256 de ProductPulse.
 *
 * Vérifie la signature, l'émetteur, l'audience, l'expiration
 * et la validité de la configuration.
 */
class JwtDecoderConfigTest {

    private static final String ISSUER = "https://auth.my-popote.example";
    private static final String AUDIENCE = "productpulse";

    private static KeyPair keyPair;
    private static String publicKeyPem;

    private final JwtDecoderConfig config = new JwtDecoderConfig();

    /**
     * Génère une paire de clés RSA dédiée aux tests.
     */
    @BeforeAll
    static void generateKeys() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        keyPair = generator.generateKeyPair();

        String encoded = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(keyPair.getPublic().getEncoded());

        publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";
    }

    @Test
    void shouldAcceptValidToken() throws Exception {
        JwtDecoder decoder = config.jwtDecoder(
                publicKeyPem, ISSUER, AUDIENCE
        );

        Jwt jwt = decoder.decode(
                createToken(ISSUER, AUDIENCE, 3600)
        );

        assertEquals("42", jwt.getSubject());
        assertEquals(ISSUER, jwt.getIssuer().toString());
        assertTrue(jwt.getAudience().contains(AUDIENCE));
    }

    @Test
    void shouldRejectWrongIssuer() throws Exception {
        JwtDecoder decoder = config.jwtDecoder(
                publicKeyPem, ISSUER, AUDIENCE
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(
                        createToken(
                                "https://unknown-issuer.example",
                                AUDIENCE,
                                3600
                        )
                )
        );
    }

    @Test
    void shouldRejectWrongAudience() throws Exception {
        JwtDecoder decoder = config.jwtDecoder(
                publicKeyPem, ISSUER, AUDIENCE
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(
                        createToken(ISSUER, "other-app", 3600)
                )
        );
    }

    @Test
    void shouldRejectExpiredToken() throws Exception {
        JwtDecoder decoder = config.jwtDecoder(
                publicKeyPem, ISSUER, AUDIENCE
        );

        assertThrows(
                JwtException.class,
                () -> decoder.decode(
                        createToken(ISSUER, AUDIENCE, -3600)
                )
        );
    }

    @Test
    void shouldRejectInvalidSignature() throws Exception {
        JwtDecoder decoder = config.jwtDecoder(
                publicKeyPem, ISSUER, AUDIENCE
        );

        KeyPair originalKeyPair = keyPair;

        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            keyPair = generator.generateKeyPair();

            String token = createToken(ISSUER, AUDIENCE, 3600);

            assertThrows(
                    JwtException.class,
                    () -> decoder.decode(token)
            );
        } finally {
            keyPair = originalKeyPair;
        }
    }

    @Test
    void shouldRejectMissingPublicKey() {
        assertThrows(
                IllegalArgumentException.class,
                () -> config.jwtDecoder("", ISSUER, AUDIENCE)
        );
    }

    @Test
    void shouldRejectMissingIssuer() {
        assertThrows(
                IllegalArgumentException.class,
                () -> config.jwtDecoder(publicKeyPem, "", AUDIENCE)
        );
    }

    @Test
    void shouldRejectMissingAudience() {
        assertThrows(
                IllegalArgumentException.class,
                () -> config.jwtDecoder(publicKeyPem, ISSUER, "")
        );
    }

    @Test
    void shouldRejectNullConfiguration() {
        assertAll(
                () -> assertThrows(
                        NullPointerException.class,
                        () -> config.jwtDecoder(null, ISSUER, AUDIENCE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> config.jwtDecoder(publicKeyPem, null, AUDIENCE)
                ),
                () -> assertThrows(
                        NullPointerException.class,
                        () -> config.jwtDecoder(publicKeyPem, ISSUER, null)
                )
        );
    }

    /**
     * Génère un JWT signé RS256 pour les scénarios de test.
     *
     * @param issuer émetteur du jeton
     * @param audience destinataire du jeton
     * @param expirationOffsetSeconds décalage de l'expiration en secondes
     * @return JWT signé et sérialisé
     */
    private static String createToken(
            String issuer,
            String audience,
            long expirationOffsetSeconds
    ) throws Exception {

        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("42")
                .issuer(issuer)
                .audience(audience)
                .issueTime(Date.from(now.minusSeconds(60)))
                .expirationTime(
                        Date.from(now.plusSeconds(expirationOffsetSeconds))
                )
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        signedJwt.sign(
                new RSASSASigner(keyPair.getPrivate())
        );

        return signedJwt.serialize();
    }
}
