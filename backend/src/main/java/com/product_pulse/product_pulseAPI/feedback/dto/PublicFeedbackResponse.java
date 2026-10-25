package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;

import java.time.Instant;
import java.util.Objects;

/**
 * Représentation d'un feedback publié pour les utilisateurs.
 *
 * Ne contient aucune donnée permettant d'identifier
 * directement l'auteur du feedback.
 *
 * Seuls les feedbacks approuvés doivent être convertis
 * dans cette représentation.
 *
 * @param id identifiant du feedback
 * @param title titre du feedback
 * @param description description du feedback
 * @param applicationCode application concernée
 * @param createdAt date de création
 */
public record PublicFeedbackResponse(
        Long id,
        String title,
        String description,
        String applicationCode,
        Instant createdAt
) {

    /**
     * Convertit un feedback approuvé en réponse publique.
     *
     * @param feedback feedback approuvé à convertir
     * @return représentation publique sans identifiant d'auteur
     * @throws IllegalArgumentException si le feedback n'est pas approuvé
     */
    public static PublicFeedbackResponse from(Feedback feedback) {
        Objects.requireNonNull(feedback, "feedback");

        if (feedback.getStatus()
                != com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus.APPROVED
                && feedback.getStatus()
                != com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Seuls les feedbacks approuvés peuvent être publiés"
            );
        }

        return new PublicFeedbackResponse(
                feedback.getId(),
                feedback.getTitle(),
                feedback.getDescription(),
                feedback.getApplicationCode(),
                feedback.getCreatedAt()
        );
    }
}
