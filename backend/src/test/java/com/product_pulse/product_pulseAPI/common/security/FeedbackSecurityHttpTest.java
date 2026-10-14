
package com.product_pulse.product_pulseAPI.common.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la protection HTTP de l'API ProductPulse.
 *
 * Les endpoints restent inaccessibles tant que
 * l'authentification JWT fiable n'est pas configurée.
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
     * Une requête anonyme ne doit jamais créer de feedback.
     */
    @Test
    void shouldRejectAnonymousFeedbackCreation() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    /**
     * Un faux jeton Bearer ne doit pas contourner
     * la politique de refus par défaut.
     */
    @Test
    void shouldRejectFakeBearerToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .header("Authorization", "Bearer fake.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }

    /**
     * Un jeton arbitraire ne donne aucun accès
     * à l'endpoint protégé.
     */
    @Test
    void shouldRejectArbitraryBearerToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .header("Authorization", "Bearer arbitrary-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_BODY))
                .andExpect(status().isForbidden());
    }
}
