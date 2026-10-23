
package com.product_pulse.product_pulseAPI.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackModerationService;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests HTTP de sécurité de l'administration ProductPulse.
 *
 * Vérifie les accès aux opérations de modération
 * et à la consultation des feedbacks avec des JWT RS256.
 *
 * Les rôles déclarés dans le JWT ne donnent aucun privilège.
 */
@SpringBootTest(properties = "app.admin.user-ids=42")
@AutoConfigureMockMvc
@Import(AdminSecurityHttpTest.TestBeans.class)
class AdminSecurityHttpTest {

    private static final String ISSUER =
            "https://auth.my-popote.example";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeyPair testKeyPair;

    @Autowired
    private FeedbackModerationService moderationService;

    @Autowired
    private FeedbackQueryService queryService;

    @Autowired
    private FeedbackVoteRepository voteRepository;

    @BeforeEach
    void resetServiceInteractions() {
        clearInvocations(moderationService, queryService, voteRepository);
    }

    @Test
    void shouldRejectUnauthenticatedModeration() throws Exception {
        mockMvc.perform(patch("/api/admin/feedback/1/approve"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch("/api/admin/feedback/1/reject"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(moderationService);
    }

    @Test
    void shouldRejectOrdinaryUserEvenWithAdminClaim()
            throws Exception {

        String token = signedToken("99", true);

        mockMvc.perform(patch("/api/admin/feedback/1/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/api/admin/feedback/1/reject")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(moderationService);
    }

    @Test
    void shouldAllowAdministratorToApprove() throws Exception {
        Feedback feedback = feedback();
        feedback.approve();

        when(moderationService.approve(1L)).thenReturn(feedback);

        mockMvc.perform(patch("/api/admin/feedback/1/approve")
                        .header(
                                "Authorization",
                                "Bearer " + signedToken("42", false)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        verify(moderationService).approve(1L);
    }

    @Test
    void shouldAllowAdministratorToReject() throws Exception {
        Feedback feedback = feedback();
        feedback.reject();

        when(moderationService.reject(1L)).thenReturn(feedback);

        mockMvc.perform(patch("/api/admin/feedback/1/reject")
                        .header(
                                "Authorization",
                                "Bearer " + signedToken("42", false)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(moderationService).reject(1L);
    }

    @Test
    void shouldRejectUnauthenticatedFeedbackListing()
            throws Exception {

        mockMvc.perform(get("/api/admin/feedback"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    @Test
    void shouldRejectOrdinaryUserFeedbackListing()
            throws Exception {

        String token = signedToken("99", true);

        mockMvc.perform(get("/api/admin/feedback")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());

        verifyNoInteractions(queryService);
    }

    @Test
    void shouldAllowAdministratorToListFeedbacks()
            throws Exception {

        Feedback feedback = feedback();

        Page<Feedback> result =
                new PageImpl<>(List.of(feedback));

        when(queryService.findAll(null, null, null, "recent", 0, 10))
                .thenReturn(result);

        mockMvc.perform(get("/api/admin/feedback")
                        .header(
                                "Authorization",
                                "Bearer " + signedToken("42", false)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(
                        jsonPath("$.content[0].title")
                                .value("Suggestion")
                )
                .andExpect(
                        jsonPath("$.content[0].status")
                                .value("PENDING")
                );

        verify(queryService).findAll(null, null, null, "recent", 0, 10);
    }

    @Test
    void shouldRejectUnauthenticatedRoleLookup() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAdminRoleForAdministrator() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + signedToken("42", false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void shouldReturnUserRoleEvenWithForgedAdminClaim() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + signedToken("99", true)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }


    /**
     * Interdit la consultation publique sans JWT.
     */
    @Test
    void shouldRejectUnauthenticatedPublicFeedbackListing()
            throws Exception {

        mockMvc.perform(get("/api/feedback"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    /**
     * Autorise un utilisateur classique à consulter uniquement
     * les feedbacks approuvés par la modération.
     */
    @Test
    void shouldAllowOrdinaryUserToListApprovedFeedbacks()
            throws Exception {

        Feedback feedback = feedback();
        feedback.approve();

        when(queryService.findAll(
                com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus.APPROVED,
                0,
                10
        )).thenReturn(new PageImpl<>(List.of(feedback)));

        mockMvc.perform(get("/api/feedback")
                        .header(
                                "Authorization",
                                "Bearer " + signedToken("99", false)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Suggestion"))
                .andExpect(jsonPath("$.content[0].authorId").doesNotExist())
                .andExpect(jsonPath("$.content[0].status").doesNotExist());

        verify(queryService).findAll(
                com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus.APPROVED,
                0,
                10
        );
    }


    @Test
    void shouldRejectUnauthenticatedVoteCount() throws Exception {
        mockMvc.perform(get("/api/admin/feedback/1/votes/count"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldRejectOrdinaryUserVoteCount() throws Exception {
        mockMvc.perform(get("/api/admin/feedback/1/votes/count")
                        .header("Authorization",
                                "Bearer " + signedToken("99", true)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(voteRepository);
    }

    @Test
    void shouldAllowAdministratorVoteCount() throws Exception {
        when(voteRepository.countByFeedbackId(1L)).thenReturn(3L);

        mockMvc.perform(get("/api/admin/feedback/1/votes/count")
                        .header("Authorization",
                                "Bearer " + signedToken("42", false)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));

        verify(voteRepository).countByFeedbackId(1L);
    }

    private Feedback feedback() {
        return new Feedback(
                "Suggestion",
                "Amélioration",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }

    private String signedToken(
            String subject,
            boolean forgedAdminClaim
    ) throws Exception {

        Instant now = Instant.now();

        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(ISSUER)
                .audience("productpulse")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(3600)));

        if (forgedAdminClaim) {
            claims.claim("role", "ADMIN");
        }

        SignedJWT token = new SignedJWT(
                new JWSHeader(JWSAlgorithm.RS256),
                claims.build()
        );

        token.sign(new RSASSASigner(testKeyPair.getPrivate()));

        return token.serialize();
    }

    /**
     * Fournit les dépendances cryptographiques et les services
     * simulés pour les tests de sécurité HTTP.
     */
    @TestConfiguration
    static class TestBeans {

        @Bean
        KeyPair adminTestKeyPair() throws Exception {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");

            generator.initialize(2048);

            return generator.generateKeyPair();
        }

        @Bean
        @Primary
        JwtDecoder adminTestJwtDecoder(
                KeyPair adminTestKeyPair
        ) throws Exception {

            String encoded = Base64.getEncoder()
                    .encodeToString(
                            adminTestKeyPair.getPublic().getEncoded()
                    );

            String pem = "-----BEGIN PUBLIC KEY-----\n"
                    + encoded
                    + "\n-----END PUBLIC KEY-----";

            return new JwtDecoderConfig().jwtDecoder(
                    pem,
                    ISSUER,
                    "productpulse"
            );
        }

        @Bean
        @Primary
        FeedbackModerationService adminTestModerationService() {
            return mock(FeedbackModerationService.class);
        }

        @Bean
        @Primary
        FeedbackVoteRepository adminTestVoteRepository() {
            return mock(FeedbackVoteRepository.class);
        }

        @Bean
        @Primary
        FeedbackQueryService adminTestQueryService() {
            return mock(FeedbackQueryService.class);
        }
    }
}
