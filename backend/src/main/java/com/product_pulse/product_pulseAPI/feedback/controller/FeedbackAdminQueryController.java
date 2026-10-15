
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API de consultation des feedbacks pour les administrateurs.
 */
@RestController
@RequestMapping("/api/admin/feedback")
public class FeedbackAdminQueryController {

    private final FeedbackQueryService queryService;

    public FeedbackAdminQueryController(
            FeedbackQueryService queryService
    ) {
        this.queryService = queryService;
    }

    /**
     * Retourne les feedbacks paginés et filtrés.
     *
     * @param status filtre optionnel
     * @param page numéro de page
     * @param size taille de page
     * @return page de feedbacks
     */
    @GetMapping
    public ResponseEntity<Page<Feedback>> findAll(
            @RequestParam(required = false) FeedbackStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(
                queryService.findAll(status, page, size)
        );
    }
}
