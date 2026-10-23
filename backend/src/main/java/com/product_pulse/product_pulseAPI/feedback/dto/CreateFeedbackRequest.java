
package com.product_pulse.product_pulseAPI.feedback.dto;

import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;

/**
 * Données nécessaires à la création d'un feedback.
 *
 * <p>Ce DTO représente la demande métier et ne contient
 * aucune information de persistance.</p>
 *
 * @param title titre du feedback
 * @param description description détaillée du feedback
 */
public record CreateFeedbackRequest(
        String title,
        String description,
        FeedbackCategory category
) {
    /** Compatibilité avec les anciens tests et appels internes. */
    public CreateFeedbackRequest(String title, String description) {
        this(title, description, FeedbackCategory.IMPROVEMENT);
    }
}
