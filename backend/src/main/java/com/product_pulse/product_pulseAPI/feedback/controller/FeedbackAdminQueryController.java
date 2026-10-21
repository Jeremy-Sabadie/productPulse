
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API REST de consultation des feedbacks
 * réservée aux administrateurs.
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class FeedbackAdminQueryController {

    private final FeedbackQueryService queryService;
    private final FeedbackVoteRepository voteRepository;

    /**
     * Initialise le contrôleur.
     *
     * @param queryService service de consultation des feedbacks
     */
    public FeedbackAdminQueryController(
            FeedbackQueryService queryService,
            FeedbackVoteRepository voteRepository
    ) {
        this.queryService = Objects.requireNonNull(
                queryService, "queryService"
        );
        this.voteRepository = Objects.requireNonNull(voteRepository);
    }

    /**
     * Retourne les feedbacks paginés et filtrés.
     *
     * @param status filtre optionnel
     * @param page numéro de page
     * @param size taille de page
     * @return page de réponses API
     */
    /** Nombre de soutiens, accessible uniquement aux administrateurs. */
    @GetMapping("/{feedbackId}/votes/count")
    public ResponseEntity<Long> countVotes(@PathVariable Long feedbackId) {
        return ResponseEntity.ok(voteRepository.countByFeedbackId(feedbackId));
    }

    @GetMapping
    public ResponseEntity<Page<FeedbackResponse>> findAll(
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<FeedbackResponse> response = queryService
                .findAll(status, page, size)
                .map(FeedbackResponse::from);

        return ResponseEntity.ok(response);
    }
}
