package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;

import java.time.Instant;
import java.util.Objects;

/**
 * Représentation d'un feedback dans l'API REST.
 *
 * <p>Évite d'exposer directement l'entité JPA et inclut
 * les références GitHub Issues lorsqu'elles existent.</p>
 *
 * @param id identifiant du feedback
 * @param title titre du feedback
 * @param description description du feedback
 * @param applicationCode application concernée
 * @param authorId identifiant de l'auteur
 * @param createdAt date de création
 * @param status statut de modération
 * @param category catégorie métier
 * @param githubIssueNumber numéro de l'issue GitHub
 * @param githubIssueUrl adresse de l'issue GitHub
 */
public record FeedbackResponse(
        Long id,
        String title,
        String description,
        String applicationCode,
        Long authorId,
        Instant createdAt,
        FeedbackStatus status,
        FeedbackCategory category,
        Long githubIssueNumber,
        String githubIssueUrl
) {

    /**
     * Convertit une entité Feedback en réponse API.
     *
     * @param feedback entité à convertir
     * @return représentation REST du feedback
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
                feedback.getStatus(),
                feedback.getCategory(),
                feedback.getGithubIssueNumber(),
                feedback.getGithubIssueUrl()
        );
    }
}
