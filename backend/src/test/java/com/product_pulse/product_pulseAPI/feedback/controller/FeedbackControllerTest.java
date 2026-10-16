
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du contrôleur REST des feedbacks.
 *
 * Vérifie l'identité authentifiée et le contrat
 * de réponse HTTP utilisé par le frontend.
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
     * Vérifie la création d'un feedback et son DTO.
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

        ResponseEntity<FeedbackResponse> response =
                controller.createFeedback(request, jwt);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        FeedbackResponse body = response.getBody();
        assertNotNull(body);

        assertAll(
                () -> assertEquals(feedback.getId(), body.id()),
                () -> assertEquals(
                        "Nouvelle fonctionnalité",
                        body.title()
                ),
                () -> assertEquals(
                        "Ajouter un filtre",
                        body.description()
                ),
                () -> assertEquals(
                        "MY_POPOTE",
                        body.applicationCode()
                ),
                () -> assertEquals(42L, body.authorId()),
                () -> assertEquals(CREATED_AT, body.createdAt()),
                () -> assertEquals(
                        FeedbackStatus.PENDING,
                        body.status()
                )
        );

        verify(feedbackService).createFeedback(
                request, "MY_POPOTE", 42L
        );

        verifyNoMoreInteractions(feedbackService);
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
