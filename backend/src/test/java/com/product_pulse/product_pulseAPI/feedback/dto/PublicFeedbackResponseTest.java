package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie la confidentialité et la modération
 * des réponses destinées aux utilisateurs.
 */
class PublicFeedbackResponseTest {

    private Feedback feedback() {
        return new Feedback(
                "Suggestion",
                "Ajouter un filtre",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }

    @Test
    void shouldConvertApprovedFeedbackWithoutAuthorId() {
        Feedback feedback = feedback();
        feedback.approve();

        PublicFeedbackResponse response =
                PublicFeedbackResponse.from(feedback);

        assertEquals("Suggestion", response.title());

        assertFalse(
                java.util.Arrays.stream(
                        PublicFeedbackResponse.class.getRecordComponents()
                ).anyMatch(component ->
                        component.getName().equals("authorId"))
        );
    }

    @Test
    void shouldRejectPendingFeedback() {
        assertThrows(
                IllegalArgumentException.class,
                () -> PublicFeedbackResponse.from(feedback())
        );
    }

    @Test
    void shouldRejectRejectedFeedback() {
        Feedback feedback = feedback();
        feedback.reject();

        assertThrows(
                IllegalArgumentException.class,
                () -> PublicFeedbackResponse.from(feedback)
        );
    }
}
