package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.PublicFeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API REST de consultation des feedbacks publiés.
 *
 * Seuls les feedbacks approuvés par un administrateur
 * peuvent être retournés aux utilisateurs authentifiés.
 *
 * La pagination et le tri sont délégués au service
 * de consultation existant.
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackPublicQueryController {

    private final FeedbackQueryService queryService;

    /**
     * Initialise le contrôleur avec le service de consultation.
     *
     * @param queryService service de recherche des feedbacks
     */
    public FeedbackPublicQueryController(
            FeedbackQueryService queryService
    ) {
        this.queryService = Objects.requireNonNull(
                queryService, "queryService"
        );
    }

    /**
     * Retourne uniquement les feedbacks approuvés.
     *
     * Le statut APPROVED est imposé côté serveur :
     * un utilisateur ne peut pas demander les feedbacks
     * en attente ou rejetés.
     *
     * @param page numéro de page, à partir de zéro
     * @param size nombre de feedbacks par page
     * @return page de feedbacks approuvés au format API
     */
    @GetMapping
    public ResponseEntity<Page<PublicFeedbackResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<PublicFeedbackResponse> response = queryService
                .findCommunity(page, size)
                .map(PublicFeedbackResponse::from);

        return ResponseEntity.ok(response);
    }
}
