
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du contrôleur REST des feedbacks.
 *
 * <p>Vérifie la transmission de l'identité authentifiée
 * au service métier et la réponse HTTP produite.</p>
 */
class FeedbackControllerTest {

    private static final Instant CREATED_AT =
            Instant.parse("2026-10-14T16:00:00Z");

    private FeedbackService feedbackService;
    private FeedbackController controller;

    @BeforeEach
    void setUp() {
        feedbackService = mock(FeedbackService.class);
        controller = new FeedbackController(feedbackService);
    }

    /**
     * Vérifie qu'un utilisateur authentifié peut créer
     * un feedback pour l'application My Popote.
     */
    @Test
    void shouldCreateFeedbackForAuthenticatedUser() {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "Nouvelle fonctionnalité",
                "Ajouter un filtre"
        );

        Jwt jwt = jwtWithSubject("42");

        Feedback feedback = new Feedback(
                request.title(),
                request.description(),
                "MY_POPOTE",
                42L,
                CREATED_AT
        );

        when(feedbackService.createFeedback(
                request, "MY_POPOTE", 42L
        )).thenReturn(feedback);

        ResponseEntity<Map<String, Object>> response =
                controller.createFeedback(request, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("MY_POPOTE", response.getBody().get("applicationCode"));
        assertEquals(42L, response.getBody().get("authorId"));

        verify(feedbackService).createFeedback(
                request, "MY_POPOTE", 42L
        );
    }

    /**
     * Vérifie le rejet d'un identifiant JWT non numérique.
     */
    @Test
    void shouldRejectInvalidJwtSubject() {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "Titre", "Description"
        );

        Jwt jwt = jwtWithSubject("invalid");

        assertThrows(
                IllegalArgumentException.class,
                () -> controller.createFeedback(request, jwt)
        );

        verifyNoInteractions(feedbackService);
    }

    /**
     * Construit un JWT de test sans signature réelle.
     *
     * @param subject identifiant de l'utilisateur
     * @return JWT destiné exclusivement aux tests unitaires
     */
    private Jwt jwtWithSubject(String subject) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject(subject)
                .issuedAt(CREATED_AT)
                .expiresAt(CREATED_AT.plusSeconds(3600))
                .build();
    }
}
