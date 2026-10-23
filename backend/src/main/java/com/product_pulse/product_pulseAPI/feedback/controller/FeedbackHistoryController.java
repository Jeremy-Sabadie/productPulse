package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackQueryService;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * API REST de consultation de l'historique personnel.
 *
 * L'identifiant de l'auteur provient exclusivement du JWT.
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackHistoryController {

    private final FeedbackQueryService queryService;

    public FeedbackHistoryController(FeedbackQueryService queryService) {
        this.queryService = Objects.requireNonNull(
                queryService, "queryService"
        );
    }

    /**
     * Retourne les feedbacks de l'utilisateur authentifié,
     * tous statuts confondus, avec pagination.
     */
    @GetMapping("/mine")
    public ResponseEntity<Page<FeedbackResponse>> findMine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Objects.requireNonNull(jwt, "jwt");

        Long authorId;

        try {
            authorId = Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Invalid authenticated user identifier",
                    exception
            );
        }

        Page<FeedbackResponse> response = queryService
                .findMine(authorId, page, size)
                .map(FeedbackResponse::from);

        return ResponseEntity.ok(response);
    }
}
