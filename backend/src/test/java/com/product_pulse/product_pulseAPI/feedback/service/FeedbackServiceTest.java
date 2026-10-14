
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

    private static final String APPLICATION_CODE = "MY_POPOTE";
    private static final Long AUTHOR_ID = 42L;

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
     * son contenu, son auteur, son application et son horodatage.
     */
    @Test
    void shouldCreateFeedbackWhenRequestIsValid() {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "Améliorer la liste de courses",
                "Ajouter un tri alphabétique"
        );

        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.createFeedback(
                request, APPLICATION_CODE, AUTHOR_ID
        );

        assertNotNull(result);
        assertEquals(request.title(), result.getTitle());
        assertEquals(request.description(), result.getDescription());
        assertEquals(APPLICATION_CODE, result.getApplicationCode());
        assertEquals(AUTHOR_ID, result.getAuthorId());
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

        Feedback result = feedbackService.createFeedback(
                request, APPLICATION_CODE, AUTHOR_ID
        );

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
                () -> feedbackService.createFeedback(
                        null, APPLICATION_CODE, AUTHOR_ID
                )
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
                new CreateFeedbackRequest(title, "Description"),
                APPLICATION_CODE,
                AUTHOR_ID
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
     */
    @Test
    void shouldHaveNullIdBeforePersistence() {
        Feedback feedback = new Feedback(
                "Titre valide",
                "Description valide",
                APPLICATION_CODE,
                AUTHOR_ID,
                FIXED_DATE
        );

        assertNull(feedback.getId());
    }

    /**
     * Vérifie le rejet d'une application absente.
     */
    @Test
    void shouldRejectNullApplicationCode() {
        assertInvalidOrigin(null, AUTHOR_ID);
    }

    /**
     * Vérifie le rejet d'un code d'application vide.
     */
    @Test
    void shouldRejectBlankApplicationCode() {
        assertInvalidOrigin("   ", AUTHOR_ID);
    }

    /**
     * Vérifie le rejet d'un code d'application
     * dépassant la longueur maximale autorisée.
     */
    @Test
    void shouldRejectApplicationCodeLongerThan100Characters() {
        assertInvalidOrigin("A".repeat(101), AUTHOR_ID);
    }

    /**
     * Vérifie le rejet d'un auteur absent.
     */
    @Test
    void shouldRejectNullAuthorId() {
        assertInvalidOrigin(APPLICATION_CODE, null);
    }

    /**
     * Vérifie le rejet d'un identifiant d'auteur non positif.
     */
    @Test
    void shouldRejectNonPositiveAuthorId() {
        assertInvalidOrigin(APPLICATION_CODE, 0L);
        assertInvalidOrigin(APPLICATION_CODE, -1L);
    }

    /**
     * Vérifie qu'un code d'application est normalisé
     * avant enregistrement.
     */
    @Test
    void shouldTrimApplicationCode() {
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Feedback result = feedbackService.createFeedback(
                new CreateFeedbackRequest("Titre", "Description"),
                "  MY_POPOTE  ",
                AUTHOR_ID
        );

        assertEquals(APPLICATION_CODE, result.getApplicationCode());
        verify(feedbackRepository).save(any(Feedback.class));
    }

    /**
     * Vérifie qu'une demande invalide est rejetée
     * sans interaction avec le repository.
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
                () -> feedbackService.createFeedback(
                        request, APPLICATION_CODE, AUTHOR_ID
                )
        );

        verifyNoInteractions(feedbackRepository);
    }

    /**
     * Vérifie le rejet d'une origine ou d'un auteur invalide.
     *
     * @param applicationCode code de l'application
     * @param authorId identifiant de l'auteur
     */
    private void assertInvalidOrigin(
            String applicationCode,
            Long authorId
    ) {
        CreateFeedbackRequest request = new CreateFeedbackRequest(
                "Titre valide",
                "Description valide"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> feedbackService.createFeedback(
                        request, applicationCode, authorId
                )
        );

        verifyNoInteractions(feedbackRepository);
    }
}
