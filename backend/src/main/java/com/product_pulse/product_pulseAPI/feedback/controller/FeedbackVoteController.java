package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.service.FeedbackVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * API REST des soutiens aux feedbacks.
 * L'identité utilisateur provient exclusivement du JWT.
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackVoteController {

    private final FeedbackVoteService service;

    public FeedbackVoteController(FeedbackVoteService service) {
        this.service = Objects.requireNonNull(service);
    }

    /**
     * Ajoute le soutien de l'utilisateur authentifié.
     */
    @PutMapping("/{feedbackId}/vote")
    public ResponseEntity<Void> support(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal Jwt jwt) {

        service.support(feedbackId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    /**
     * Retire le soutien de l'utilisateur authentifié.
     */
    @DeleteMapping("/{feedbackId}/vote")
    public ResponseEntity<Void> removeSupport(
            @PathVariable Long feedbackId,
            @AuthenticationPrincipal Jwt jwt) {

        service.removeSupport(feedbackId, userId(jwt));
        return ResponseEntity.noContent().build();
    }


    /**
     * Identifiants des avis soutenus par l'utilisateur connecté.
     */
    @GetMapping("/my-votes")
    public java.util.List<Long> myVotes(
            @AuthenticationPrincipal Jwt jwt) {
        return service.myVotes(userId(jwt));
    }

    private Long userId(Jwt jwt) {
        if (jwt == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        try {
            return Long.valueOf(jwt.getSubject());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid authenticated user identifier",
                    exception
            );
        }
    }
}
