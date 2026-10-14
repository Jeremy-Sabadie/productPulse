
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
 * Service métier chargé de la création des feedbacks.
 *
 * <p>Valide les données avant leur enregistrement
 * et attribue l'horodatage côté serveur.</p>
 *
 * <p>L'application et l'auteur doivent provenir
 * d'une couche d'authentification fiable.</p>
 */
@Service
public class FeedbackService {

    private static final int MAX_TITLE_LENGTH = 150;
    private static final int MAX_APPLICATION_CODE_LENGTH = 100;

    private final FeedbackRepository feedbackRepository;
    private final Clock clock;

    /**
     * Initialise le service avec ses dépendances.
     *
     * @param feedbackRepository repository des feedbacks
     * @param clock horloge utilisée pour l'horodatage
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
     * @param request données saisies par l'utilisateur
     * @param applicationCode code de l'application concernée
     * @param authorId identifiant de l'utilisateur authentifié
     * @return feedback enregistré
     * @throws NullPointerException si la demande est absente
     * @throws IllegalArgumentException si une donnée est invalide
     */
    @Transactional
    public Feedback createFeedback(
            CreateFeedbackRequest request,
            String applicationCode,
            Long authorId
    ) {
        Objects.requireNonNull(request, "request");

        String title = validateRequiredText(
                request.title(), "title"
        );

        String description = validateRequiredText(
                request.description(), "description"
        );

        String validatedApplicationCode = validateRequiredText(
                applicationCode, "applicationCode"
        );

        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException(
                    "title must not exceed 150 characters"
            );
        }

        if (validatedApplicationCode.length()
                > MAX_APPLICATION_CODE_LENGTH) {
            throw new IllegalArgumentException(
                    "applicationCode must not exceed 100 characters"
            );
        }

        if (authorId == null || authorId <= 0) {
            throw new IllegalArgumentException(
                    "authorId must be positive"
            );
        }

        Feedback feedback = new Feedback(
                title,
                description,
                validatedApplicationCode,
                authorId,
                Instant.now(clock)
        );

        return feedbackRepository.save(feedback);
    }

    /**
     * Vérifie qu'un texte obligatoire est renseigné
     * et supprime les espaces périphériques.
     *
     * @param value texte à valider
     * @param fieldName nom du champ
     * @return texte normalisé
     */
    private String validateRequiredText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.strip();
    }
}
