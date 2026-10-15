
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests d'intégration de la modération avec MariaDB.
 *
 * Chaque test est exécuté dans une transaction annulée
 * automatiquement à la fin.
 */
@SpringBootTest
@Transactional
class FeedbackModerationIntegrationTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackModerationService moderationService;

    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldPersistApprovedStatus() {
        Long id = createPendingFeedback();

        Feedback moderated = moderationService.approve(id);

        assertEquals(FeedbackStatus.APPROVED, moderated.getStatus());

        entityManager.flush();
        entityManager.clear();

        Feedback reloaded = feedbackRepository.findById(id)
                .orElseThrow();

        assertEquals(FeedbackStatus.APPROVED, reloaded.getStatus());
    }

    @Test
    void shouldPersistRejectedStatus() {
        Long id = createPendingFeedback();

        Feedback moderated = moderationService.reject(id);

        assertEquals(FeedbackStatus.REJECTED, moderated.getStatus());

        entityManager.flush();
        entityManager.clear();

        Feedback reloaded = feedbackRepository.findById(id)
                .orElseThrow();

        assertEquals(FeedbackStatus.REJECTED, reloaded.getStatus());
    }

    @Test
    void shouldRejectSecondModerationDecision() {
        Long id = createPendingFeedback();

        moderationService.approve(id);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> moderationService.reject(id)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        entityManager.flush();
        entityManager.clear();

        Feedback reloaded = feedbackRepository.findById(id)
                .orElseThrow();

        assertEquals(FeedbackStatus.APPROVED, reloaded.getStatus());
    }

    /**
     * Crée un feedback en attente de modération.
     *
     * @return identifiant du feedback créé
     */
    private Long createPendingFeedback() {
        Feedback feedback = new Feedback(
                "Suggestion de test",
                "Vérification de la persistance MariaDB",
                "MY_POPOTE",
                42L,
                Instant.parse("2026-10-15T12:00:00Z")
        );

        Feedback saved = feedbackRepository.saveAndFlush(feedback);

        assertEquals(FeedbackStatus.PENDING, saved.getStatus());

        entityManager.clear();

        return saved.getId();
    }
}
