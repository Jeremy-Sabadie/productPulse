
package com.product_pulse.product_pulseAPI.feedback.controller;

import com.product_pulse.product_pulseAPI.feedback.dto.CreateFeedbackRequest;
import com.product_pulse.product_pulseAPI.feedback.dto.FeedbackResponse;
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

import java.util.Objects;

/**
 * API REST de création des feedbacks ProductPulse.
 *
 * L'identifiant de l'auteur provient exclusivement
 * du JWT authentifié.
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
     * @param jwt jeton authentifié
     * @return réponse HTTP 201 contenant le feedback créé
     */
    @PostMapping
    public ResponseEntity<FeedbackResponse> createFeedback(
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

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(FeedbackResponse.from(feedback));
    }
}
