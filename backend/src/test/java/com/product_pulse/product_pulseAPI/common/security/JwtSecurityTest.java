
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
 * Tests de sécurité HTTP de l'authentification JWT.
 *
 * Un utilisateur anonyme ou présentant un jeton invalide
 * ne doit jamais pouvoir créer de feedback.
 */
@SpringBootTest
@AutoConfigureMockMvc
class JwtSecurityTest {

    private static final String FEEDBACK_URL = "/api/feedback";

    private static final String FEEDBACK_BODY = """
            {
              "title": "Suggestion",
              "description": "Ajouter un filtre"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    /**
     * Refuse une requête sans jeton.
     */
    @Test
    void shouldRejectRequestWithoutToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FEEDBACK_BODY))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Refuse un jeton Bearer malformé.
     */
    @Test
    void shouldRejectMalformedToken() throws Exception {
        mockMvc.perform(post(FEEDBACK_URL)
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(FEEDBACK_BODY))
                .andExpect(status().isUnauthorized());
    }
}
