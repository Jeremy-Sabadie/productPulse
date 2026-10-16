
package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;

import java.time.Instant;
import java.util.Objects;

/**
 * Représentation publique d'un feedback dans l'API REST.
 *
 * Ce DTO évite d'exposer directement l'entité JPA.
 *
 * @param id identifiant du feedback
 * @param title titre du feedback
 * @param description description du feedback
 * @param applicationCode application concernée
 * @param authorId identifiant de l'auteur
 * @param createdAt date de création
 * @param status statut de modération
 */
public record FeedbackResponse(
        Long id,
        String title,
        String description,
        String applicationCode,
        Long authorId,
        Instant createdAt,
        FeedbackStatus status
) {

    /**
     * Convertit une entité Feedback en réponse API.
     *
     * @param feedback entité à convertir
     * @return DTO contenant les données du feedback
     */
    public static FeedbackResponse from(Feedback feedback) {
        Objects.requireNonNull(feedback, "feedback");

        return new FeedbackResponse(
                feedback.getId(),
                feedback.getTitle(),
                feedback.getDescription(),
                feedback.getApplicationCode(),
                feedback.getAuthorId(),
                feedback.getCreatedAt(),
                feedback.getStatus()
        );
    }
}
