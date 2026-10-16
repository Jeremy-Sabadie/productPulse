
package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackResponseTest {

    @Test
    void shouldConvertFeedbackEntityToResponse() {
        Instant createdAt =
                Instant.parse("2026-10-16T12:00:00Z");

        Feedback feedback = new Feedback(
                "Nouvelle fonctionnalité",
                "Ajouter un filtre par catégorie",
                "MY_POPOTE",
                42L,
                createdAt
        );

        FeedbackResponse response =
                FeedbackResponse.from(feedback);

        assertAll(
                () -> assertNull(response.id()),
                () -> assertEquals(
                        "Nouvelle fonctionnalité",
                        response.title()
                ),
                () -> assertEquals(
                        "Ajouter un filtre par catégorie",
                        response.description()
                ),
                () -> assertEquals(
                        "MY_POPOTE",
                        response.applicationCode()
                ),
                () -> assertEquals(42L, response.authorId()),
                () -> assertEquals(createdAt, response.createdAt()),
                () -> assertEquals(
                        FeedbackStatus.PENDING,
                        response.status()
                )
        );
    }

    @Test
    void shouldRejectNullFeedback() {
        assertThrows(
                NullPointerException.class,
                () -> FeedbackResponse.from(null)
        );
    }
}
