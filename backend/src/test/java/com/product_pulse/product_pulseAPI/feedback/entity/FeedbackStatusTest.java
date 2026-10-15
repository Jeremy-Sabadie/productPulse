
package com.product_pulse.product_pulseAPI.feedback.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Vérifie les statuts disponibles pour la modération
 * des retours utilisateurs.
 */
class FeedbackStatusTest {

    @Test
    void shouldExposeAllModerationStatuses() {
        assertArrayEquals(
                new FeedbackStatus[]{
                        FeedbackStatus.PENDING,
                        FeedbackStatus.APPROVED,
                        FeedbackStatus.REJECTED
                },
                FeedbackStatus.values()
        );
    }

    @Test
    void shouldResolveStatusByName() {
        assertEquals(
                FeedbackStatus.PENDING,
                FeedbackStatus.valueOf("PENDING")
        );
    }
}
