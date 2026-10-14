
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.service.FeedbackService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * API REST de création des feedbacks ProductPulse.
 *
 * <p>L'identifiant de l'auteur est extrait du principal
 * authentifié et n'est jamais accepté depuis le corps HTTP.</p>
 */
@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private static final String APPLICATION_CODE = "MY_POPOTE";

    private final FeedbackService feedbackService;

    /**
     * Initialise le contrôleur.
     *
     * @param feedbackService service métier des feedbacks
     */
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = Objects.requireNonNull(
                feedbackService, "feedbackService"
        );
    }

    /**
     * Enregistre un feedback pour l'utilisateur authentifié.
     *
     * @param request données du feedback
     * @param jwt jeton préalablement validé par Spring Security
     * @return réponse HTTP 201 contenant le feedback créé
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createFeedback(
            @RequestBody CreateFeedbackRequest request,
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

        Feedback feedback = feedbackService.createFeedback(
                request,
                APPLICATION_CODE,
                authorId
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", feedback.getId());
        response.put("title", feedback.getTitle());
        response.put("description", feedback.getDescription());
        response.put("applicationCode", feedback.getApplicationCode());
        response.put("authorId", feedback.getAuthorId());
        response.put("createdAt", feedback.getCreatedAt());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
