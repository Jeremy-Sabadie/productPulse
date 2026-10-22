
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * API REST de consultation réservée aux administrateurs.
 *
 * Les autorisations sont appliquées côté serveur
 * par la configuration Spring Security existante.
 *
 * Les filtres de l'interface Angular ne constituent
 * jamais un mécanisme de sécurité.
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class FeedbackAdminQueryController {

    private final FeedbackQueryService queryService;
    private final FeedbackVoteRepository voteRepository;

    public FeedbackAdminQueryController(
            FeedbackQueryService queryService,
            FeedbackVoteRepository voteRepository
    ) {
        this.queryService = Objects.requireNonNull(
                queryService, "queryService"
        );
        this.voteRepository = Objects.requireNonNull(
                voteRepository, "voteRepository"
        );
    }

    /**
     * Nombre de soutiens d'un avis.
     *
     * Le compteur est réservé à l'administration,
     * conformément aux règles métier de ProductPulse.
     */
    @GetMapping("/{feedbackId}/votes/count")
    public ResponseEntity<Long> countVotes(
            @PathVariable Long feedbackId
    ) {
        return ResponseEntity.ok(
                voteRepository.countByFeedbackId(feedbackId)
        );
    }

    /**
     * Liste paginée des avis avec filtres facultatifs.
     *
     * Exemples :
     * GET /api/admin/feedback?status=PENDING
     * GET /api/admin/feedback?applicationCode=MY_POPOTE
     * GET /api/admin/feedback?status=APPROVED&sort=votes_desc
     *
     * Les paramètres sont validés dans la couche service.
     */
    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> findAll(
            @RequestParam(required = false)
            FeedbackStatus status,

            @RequestParam(required = false)
            String applicationCode,

            @RequestParam(defaultValue = "recent")
            String sort,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size
    ) {
        Page<FeedbackResponse> response = queryService
                .findAll(
                        status,
                        applicationCode,
                        sort,
                        page,
                        size
                )
                .map(FeedbackResponse::from);

        return ResponseEntity.ok(response);
    }
}
