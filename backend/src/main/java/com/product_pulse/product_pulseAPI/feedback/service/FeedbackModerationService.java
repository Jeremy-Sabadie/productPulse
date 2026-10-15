
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Objects;

/**
 * Gère les décisions de modération des feedbacks.
 */
@Service
public class FeedbackModerationService {

    private final FeedbackRepository repository;

    /**
     * Initialise le service de modération.
     *
     * @param repository accès aux feedbacks
     */
    public FeedbackModerationService(FeedbackRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Approuve un feedback en attente.
     *
     * @param id identifiant du feedback
     * @return feedback approuvé
     */
    @Transactional
    public Feedback approve(Long id) {
        Feedback feedback = findFeedback(id);

        try {
            feedback.approve();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Feedback already moderated",
                    exception
            );
        }

        return feedback;
    }

    /**
     * Refuse un feedback en attente.
     *
     * @param id identifiant du feedback
     * @return feedback refusé
     */
    @Transactional
    public Feedback reject(Long id) {
        Feedback feedback = findFeedback(id);

        try {
            feedback.reject();
        } catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Feedback already moderated",
                    exception
            );
        }

        return feedback;
    }

    private Feedback findFeedback(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid feedback identifier"
            );
        }

        return repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Feedback not found"
                )
        );
    }
}
