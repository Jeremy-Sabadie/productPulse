
package com.product_pulse.product_pulseAPI.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackService;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackVoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test d'intégration HTTP avec authentification JWT RS256 réelle.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthenticatedFeedbackHttpTest.TestSecurityBeans.class)
class AuthenticatedFeedbackHttpTest {

    private static final String ISSUER = "https://auth.my-popote.example";
    private static final String AUDIENCE = "productpulse";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackVoteService feedbackVoteService;

    @Autowired
    private KeyPair testKeyPair;

    /**
     * Vérifie qu'un utilisateur authentifié peut créer un feedback
     * et que son identifiant est extrait du JWT.
     */
    @Test
    void shouldCreateFeedbackWithValidJwt() throws Exception {
        Feedback feedback = new Feedback(
                "Suggestion",
                "Ajouter un filtre",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );

        when(feedbackService.createFeedback(
                any(CreateFeedbackRequest.class),
                eq("MY_POPOTE"),
                eq(42L)
        )).thenReturn(feedback);

        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + createToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Suggestion",
                                  "description": "Ajouter un filtre"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Suggestion"))
                .andExpect(jsonPath("$.authorId").value(42))
                .andExpect(jsonPath("$.applicationCode").value("MY_POPOTE"));

        verify(feedbackService).createFeedback(
                any(CreateFeedbackRequest.class),
                eq("MY_POPOTE"),
                eq(42L)
        );
    }

    /**
     * Produit un JWT réellement signé en RS256.
     *
     * @return jeton Bearer utilisable par MockMvc
     */

    /**
     * Vérifie qu'un JWT valide autorise le soutien d'un avis.
     */
    @Test
    void shouldSupportFeedbackWithValidJwt() throws Exception {
        mockMvc.perform(put("/api/feedback/1/vote")
                        .header("Authorization", "Bearer " + createToken()))
                .andExpect(status().isNoContent());

        verify(feedbackVoteService).support(1L, 42L);
    }

    /**
     * Vérifie qu'un utilisateur peut retirer son soutien.
     */
    @Test
    void shouldRemoveSupportWithValidJwt() throws Exception {
        mockMvc.perform(delete("/api/feedback/1/vote")
                        .header("Authorization", "Bearer " + createToken()))
                .andExpect(status().isNoContent());

        verify(feedbackVoteService).removeSupport(1L, 42L);
    }

    /**
     * Vérifie que seuls les votes de l'utilisateur connecté
     * sont demandés au service.
     */
    @Test
    void shouldRetrieveOwnVotesWithValidJwt() throws Exception {
        when(feedbackVoteService.myVotes(42L))
                .thenReturn(java.util.List.of(1L, 3L));

        mockMvc.perform(get("/api/feedback/my-votes")
                        .header("Authorization", "Bearer " + createToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(1))
                .andExpect(jsonPath("$[1]").value(3));

        verify(feedbackVoteService).myVotes(42L);
    }

    private String createToken() throws Exception {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("42")
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)))
                .build();

        SignedJWT token = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims
        );

        token.sign(new RSASSASigner(testKeyPair.getPrivate()));

        return token.serialize();
    }

    /**
     * Fournit une paire RSA de test et son décodeur associé.
     */
    @TestConfiguration
    static class TestSecurityBeans {

        @Bean
        KeyPair testKeyPair() throws Exception {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }

        @Bean
        @Primary
        JwtDecoder testJwtDecoder(KeyPair testKeyPair)
                throws Exception {

            String encoded = java.util.Base64.getEncoder()
                    .encodeToString(testKeyPair.getPublic().getEncoded());

            String pem = "-----BEGIN PUBLIC KEY-----\n"
                    + encoded
                    + "\n-----END PUBLIC KEY-----";

            return new JwtDecoderConfig().jwtDecoder(
                    pem,
                    ISSUER,
                    AUDIENCE
            );
        }

        @Bean
        @Primary
        FeedbackVoteService testFeedbackVoteService() {
            return mock(FeedbackVoteService.class);
        }

        @Bean
        @Primary
        FeedbackService testFeedbackService() {
            return mock(FeedbackService.class);
        }
    }
}
