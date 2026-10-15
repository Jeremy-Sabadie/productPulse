
package com.product_pulse.product_pulseAPI.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie le refus des requêtes HTTP non authentifiées
 * sur l'API ProductPulse.
 */
@SpringBootTest
@AutoConfigureMockMvc
class FeedbackSecurityHttpTest {

    private static final String FEEDBACK_URL = "/api/feedback";

    private static final String VALID_BODY = """
            {
              "title": "Suggestion",
              "description": "Ajouter un filtre"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Refuse la création de feedback sans authentification.
     */
    @Test
    void shouldRejectAnonymousFeedbackCreation() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Refuse un JWT Bearer malformé.
     */
    @Test
    void shouldRejectFakeBearerToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .header("Authorization", "Bearer fake.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Refuse un jeton arbitraire non vérifiable.
     */
    @Test
    void shouldRejectArbitraryBearerToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .header("Authorization", "Bearer arbitrary-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isUnauthorized());
    }
}
