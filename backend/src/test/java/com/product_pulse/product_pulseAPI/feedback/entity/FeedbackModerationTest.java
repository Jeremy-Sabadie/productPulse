
package com.product_pulse.product_pulseAPI.feedback.entity;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FeedbackModerationTest {

    private Feedback newFeedback() {
        return new Feedback(
                "Amélioration",
                "Ajouter un filtre aux recettes",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );
    }

    @Test
    void newFeedbackShouldBePending() {
        Feedback feedback = newFeedback();

        assertEquals(FeedbackStatus.PENDING, feedback.getStatus());
    }

    @Test
    void pendingFeedbackCanBeApproved() {
        Feedback feedback = newFeedback();

        feedback.approve();

        assertEquals(FeedbackStatus.APPROVED, feedback.getStatus());
    }

    @Test
    void pendingFeedbackCanBeRejected() {
        Feedback feedback = newFeedback();

        feedback.reject();

        assertEquals(FeedbackStatus.REJECTED, feedback.getStatus());
    }

    @Test
    void approvedFeedbackCannotBeRejected() {
        Feedback feedback = newFeedback();
        feedback.approve();

        assertThrows(
                IllegalStateException.class,
                feedback::reject
        );
    }

    @Test
    void rejectedFeedbackCannotBeApproved() {
        Feedback feedback = newFeedback();
        feedback.reject();

        assertThrows(
                IllegalStateException.class,
                feedback::approve
        );
    }

    @Test
    void feedbackCannotBeApprovedTwice() {
        Feedback feedback = newFeedback();
        feedback.approve();

        assertThrows(
                IllegalStateException.class,
                feedback::approve
        );
    }

    @Test
    void feedbackCannotBeRejectedTwice() {
        Feedback feedback = newFeedback();
        feedback.reject();

        assertThrows(
                IllegalStateException.class,
                feedback::reject
        );
    }
}
