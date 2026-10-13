
package com.product_pulse.product_pulseAPI.feedback.dto;

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
        String description
) {
}
