
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/**
 * Service applicatif responsable de la création des feedbacks.
 *
 * <p>Il centralise les règles de validation et délègue
 * exclusivement la persistance au repository.</p>
 */
@Service
public class FeedbackService {

    private static final int MAX_TITLE_LENGTH = 150;

    private final FeedbackRepository feedbackRepository;
    private final Clock clock;

    /**
     * Initialise le service avec ses dépendances.
     *
     * @param feedbackRepository repository de persistance
     * @param clock horloge utilisée pour dater les feedbacks
     */
    public FeedbackService(
            FeedbackRepository feedbackRepository,
            Clock clock
    ) {
        this.feedbackRepository = Objects.requireNonNull(
                feedbackRepository, "feedbackRepository"
        );
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    /**
     * Valide et enregistre un nouveau feedback.
     *
     * @param request données du feedback
     * @return feedback enregistré
     * @throws NullPointerException si la demande est null
     * @throws IllegalArgumentException si les données sont invalides
     */
    @Transactional
    public Feedback createFeedback(CreateFeedbackRequest request) {
        Objects.requireNonNull(request, "request");

        String title = validateRequiredText(
                request.title(), "Le titre"
        );

        String description = validateRequiredText(
                request.description(), "La description"
        );

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "Le titre ne doit pas dépasser 150 caractères"
            );
        }

        Instant createdAt = Instant.now(clock);

        Feedback feedback = new Feedback(
                title,
                description,
                createdAt
        );

        return feedbackRepository.save(feedback);
    }

    /**
     * Vérifie qu'un texte obligatoire est renseigné,
     * puis supprime ses espaces périphériques.
     *
     * @param value texte à contrôler
     * @param fieldName nom du champ pour le message d'erreur
     * @return texte normalisé
     */
    private String validateRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " est obligatoire"
            );
        }

        return value.strip();
    }
}
