
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackModerationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires du contrôleur de modération.
 *
 * Vérifie les réponses HTTP et la délégation
 * des décisions au service métier.
 */
class FeedbackModerationControllerTest {

    private FeedbackModerationService moderationService;
    private FeedbackModerationController controller;

    @BeforeEach
    void setUp() {
        moderationService = mock(FeedbackModerationService.class);
        controller = new FeedbackModerationController(moderationService);
    }

    @Test
    void shouldApproveFeedback() {
        Feedback feedback = newFeedback();
        feedback.approve();

        when(moderationService.approve(1L)).thenReturn(feedback);

        ResponseEntity<FeedbackModerationController.ModerationResponse>
                response = controller.approve(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FeedbackStatus.APPROVED, response.getBody().status());
        assertEquals(feedback.getId(), response.getBody().id());

        verify(moderationService).approve(1L);
        verifyNoMoreInteractions(moderationService);
    }

    @Test
    void shouldRejectFeedback() {
        Feedback feedback = newFeedback();
        feedback.reject();

        when(moderationService.reject(2L)).thenReturn(feedback);

        ResponseEntity<FeedbackModerationController.ModerationResponse>
                response = controller.reject(2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(FeedbackStatus.REJECTED, response.getBody().status());
        assertEquals(feedback.getId(), response.getBody().id());

        verify(moderationService).reject(2L);
        verifyNoMoreInteractions(moderationService);
    }

    private Feedback newFeedback() {
        return new Feedback(
                "Amélioration",
                "Ajouter un filtre",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }
}
