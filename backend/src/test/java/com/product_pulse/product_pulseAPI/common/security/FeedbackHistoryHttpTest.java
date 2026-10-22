package com.product_pulse.product_pulseAPI.common.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Date;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(FeedbackHistoryHttpTest.TestSecurityBeans.class)
class FeedbackHistoryHttpTest {

    private static final String ISSUER =
            "https://auth.my-popote.example";

    private static final String AUDIENCE = "productpulse";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FeedbackQueryService queryService;

    @Autowired
    private KeyPair testKeyPair;

    @Test
    void shouldRejectUnauthenticatedHistoryRequest() throws Exception {
        mockMvc.perform(get("/api/feedback/mine"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(queryService);
    }

    @Test
    void shouldReturnAuthenticatedUsersHistory() throws Exception {
        when(queryService.findMine(42L, 0, 10))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/feedback/mine")
                        .header(
                                "Authorization",
                                "Bearer " + createToken("42")
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty());

        verify(queryService).findMine(42L, 0, 10);
    }

    @Test
    void shouldUseAuthenticatedIdentityAndPagination() throws Exception {
        when(queryService.findMine(73L, 2, 5))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/feedback/mine")
                        .param("page", "2")
                        .param("size", "5")
                        .header(
                                "Authorization",
                                "Bearer " + createToken("73")
                        ))
                .andExpect(status().isOk());

        verify(queryService).findMine(73L, 2, 5);
    }

    private String createToken(String subject) throws Exception {
        Instant now = Instant.now();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(subject)
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

    @TestConfiguration
    static class TestSecurityBeans {

        @Bean
        KeyPair historyTestKeyPair() throws Exception {
            KeyPairGenerator generator =
                    KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }

        @Bean
        @Primary
        JwtDecoder historyTestJwtDecoder(KeyPair historyTestKeyPair)
                throws Exception {

            String encoded = java.util.Base64.getEncoder()
                    .encodeToString(
                            historyTestKeyPair.getPublic().getEncoded()
                    );

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
        FeedbackQueryService historyTestQueryService() {
            return mock(FeedbackQueryService.class);
        }
    }
}
