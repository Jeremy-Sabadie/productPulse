
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackModerationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API REST de modération réservée aux administrateurs.
 *
 * Les autorisations sont appliquées par Spring Security.
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class FeedbackModerationController {

    private final FeedbackModerationService moderationService;

    /**
     * Initialise le contrôleur.
     *
     * @param moderationService service de modération
     */
    public FeedbackModerationController(
            FeedbackModerationService moderationService
    ) {
        this.moderationService = Objects.requireNonNull(
                moderationService,
                "moderationService"
        );
    }

    /**
     * Approuve un feedback.
     *
     * @param id identifiant du feedback
     * @return décision enregistrée
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ModerationResponse> approve(
            @PathVariable Long id
    ) {
        Feedback feedback = moderationService.approve(id);

        return ResponseEntity.ok(
                new ModerationResponse(
                        feedback.getId(),
                        feedback.getStatus()
                )
        );
    }

    /**
     * Refuse un feedback.
     *
     * @param id identifiant du feedback
     * @return décision enregistrée
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ModerationResponse> reject(
            @PathVariable Long id
    ) {
        Feedback feedback = moderationService.reject(id);

        return ResponseEntity.ok(
                new ModerationResponse(
                        feedback.getId(),
                        feedback.getStatus()
                )
        );
    }

    /**
     * Réponse minimale d'une décision de modération.
     *
     * @param id identifiant du feedback
     * @param status nouveau statut
     */
    public record ModerationResponse(
            Long id,
            FeedbackStatus status
    ) {
    }
}
