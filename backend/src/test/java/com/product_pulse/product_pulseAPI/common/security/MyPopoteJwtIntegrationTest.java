
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie le contrat JWT partagé entre My Popote et ProductPulse.
 */
class MyPopoteJwtIntegrationTest {

    private static final String ISSUER = "https://auth.my-popote.example";
    private static final String MY_POPOTE = "mypopote";
    private static final String PRODUCTPULSE = "productpulse";

    private static KeyPair keyPair;
    private static JwtDecoder decoder;

    /**
     * Prépare une paire RSA temporaire et le décodeur ProductPulse.
     */
    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        String encoded = Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(keyPair.getPublic().getEncoded());

        String publicKeyPem = "-----BEGIN PUBLIC KEY-----\n"
                + encoded
                + "\n-----END PUBLIC KEY-----";

        decoder = new JwtDecoderConfig().jwtDecoder(
                publicKeyPem, ISSUER, PRODUCTPULSE
        );
    }

    /**
     * Accepte un jeton autorisé pour les deux applications.
     */
    @Test
    void shouldAcceptTokenForBothApplications() throws Exception {
        Jwt jwt = decoder.decode(
                createToken(List.of(MY_POPOTE, PRODUCTPULSE))
        );

        assertEquals("42", jwt.getSubject());
        assertTrue(jwt.getAudience().contains(MY_POPOTE));
        assertTrue(jwt.getAudience().contains(PRODUCTPULSE));
    }

    /**
     * Refuse un jeton destiné uniquement à My Popote.
     */
    @Test
    void shouldRejectTokenForMyPopoteOnly() throws Exception {
        String token = createToken(List.of(MY_POPOTE));

        assertThrows(
                JwtException.class,
                () -> decoder.decode(token)
        );
    }

    /**
     * Génère un JWT RS256 représentatif du contrat d'authentification.
     *
     * @param audiences applications destinataires
     * @return jeton signé
     */
    private static String createToken(List<String> audiences)
            throws Exception {

        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("42")
                .issuer(ISSUER)
                .audience(audiences)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .build();

        SignedJWT signedJwt = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        signedJwt.sign(new RSASSASigner(keyPair.getPrivate()));

        return signedJwt.serialize();
    }
}
