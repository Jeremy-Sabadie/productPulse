
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires des règles métier de création des feedbacks.
 *
 * <p>Le repository est simulé avec Mockito afin d'isoler
 * la logique métier de la couche de persistance.</p>
 *
 * <p>Une horloge fixe garantit des tests déterministes.</p>
 */
@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    private static final Instant FIXED_DATE =
            Instant.parse("2026-10-13T10:00:00Z");

    @Mock
    private FeedbackRepository feedbackRepository;

    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(
                FIXED_DATE,
                ZoneOffset.UTC
        );

        feedbackService = new FeedbackService(
                feedbackRepository,
                fixedClock
        );
    }

    /**
     * Vérifie la création d'un feedback valide,
     * son contenu et son horodatage.
     */
    @Test
    void shouldCreateFeedbackWhenRequestIsValid() {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "Améliorer la liste de courses",
                "Ajouter un tri alphabétique"
        );

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.createFeedback(request);

        assertNotNull(result);
        assertEquals(request.title(), result.getTitle());
        assertEquals(request.description(), result.getDescription());
        assertEquals(FIXED_DATE, result.getCreatedAt());

        verify(feedbackRepository).save(any(Feedback.class));
    }

    /**
     * Vérifie la suppression des espaces périphériques
     * avant l'enregistrement du feedback.
     */
    @Test
    void shouldTrimTitleAndDescription() {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "  Mon titre  ",
                "  Ma description  "
        );

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.createFeedback(request);

        assertEquals("Mon titre", result.getTitle());
        assertEquals("Ma description", result.getDescription());

        verify(feedbackRepository).save(any(Feedback.class));
    }

    /**
     * Vérifie qu'une demande absente est rejetée
     * avant toute interaction avec la persistance.
     */
    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                NullPointerException.class,
                () -> feedbackService.createFeedback(null)
        );

        verifyNoInteractions(feedbackRepository);
    }

    @Test
    void shouldRejectNullTitle() {
        assertInvalidRequest(null, "Description valide");
    }

    @Test
    void shouldRejectBlankTitle() {
        assertInvalidRequest("   ", "Description valide");
    }

    @Test
    void shouldRejectNullDescription() {
        assertInvalidRequest("Titre valide", null);
    }

    @Test
    void shouldRejectBlankDescription() {
        assertInvalidRequest("Titre valide", "   ");
    }

    /**
     * Vérifie que la longueur maximale autorisée
     * pour un titre est acceptée.
     */
    @Test
    void shouldAcceptTitleWith150Characters() {
        String title = "A".repeat(150);

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.createFeedback(
                new CreateFeedbackRequest(title, "Description")
        );

        assertEquals(150, result.getTitle().length());

        verify(feedbackRepository).save(any(Feedback.class));
    }

    /**
     * Vérifie le rejet d'un titre dépassant
     * la longueur maximale autorisée.
     */
    @Test
    void shouldRejectTitleLongerThan150Characters() {
        assertInvalidRequest(
                "A".repeat(151),
                "Description valide"
        );
    }

    /**
     * Vérifie qu'une entité nouvellement construite
     * ne possède pas encore d'identifiant de persistance.
     *
     * <p>L'identifiant est attribué par la base de données
     * lors de l'enregistrement.</p>
     */
    @Test
    void shouldHaveNullIdBeforePersistence() {
        Feedback feedback = new Feedback(
                "Titre valide",
                "Description valide",
                FIXED_DATE
        );

        assertNull(feedback.getId());
    }

    /**
     * Vérifie qu'une demande invalide est rejetée
     * sans aucune interaction avec le repository.
     *
     * @param title titre à tester
     * @param description description à tester
     */
    private void assertInvalidRequest(
            String title,
            String description
    ) {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                title,
                description
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> feedbackService.createFeedback(request)
        );

        verifyNoInteractions(feedbackRepository);
    }
}
