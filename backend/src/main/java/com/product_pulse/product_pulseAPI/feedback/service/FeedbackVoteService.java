package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackVote;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackVoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

/**
 * Gère les soutiens aux feedbacks approuvés.
 */
@Service
public class FeedbackVoteService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackVoteRepository voteRepository;

    public FeedbackVoteService(
            FeedbackRepository feedbackRepository,
            FeedbackVoteRepository voteRepository) {
        this.feedbackRepository = Objects.requireNonNull(feedbackRepository);
        this.voteRepository = Objects.requireNonNull(voteRepository);
    }

    /**
     * Ajoute un soutien unique.
     */
    @Transactional
    public void support(Long feedbackId, Long userId) {
        validate(feedbackId, userId);

        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Feedback not found"));

        if (feedback.getStatus() != FeedbackStatus.APPROVED && feedback.getStatus() != FeedbackStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Feedback not approved");
        }

        if (voteRepository.existsByFeedbackIdAndUserId(feedbackId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Already supported");
        }

        try {
            voteRepository.save(new FeedbackVote(feedback, userId));
            voteRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Already supported", exception);
        }
    }

    /**
     * Retire uniquement le soutien de l'utilisateur.
     */
    @Transactional
    public void removeSupport(Long feedbackId, Long userId) {
        validate(feedbackId, userId);

        if (!voteRepository.existsByFeedbackIdAndUserId(feedbackId, userId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Vote not found");
        }

        voteRepository.deleteByFeedbackIdAndUserId(feedbackId, userId);
    }


    /**
     * Liste les avis approuvés soutenus par l'utilisateur.
     */
    @Transactional(readOnly = true)
    public java.util.List<Long> myVotes(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid user identifier");
        }

        return voteRepository.findFeedbackIdsByUserIdAndStatus(
                userId, FeedbackStatus.APPROVED);
    }

    private void validate(Long feedbackId, Long userId) {
        if (feedbackId == null || feedbackId <= 0
                || userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid identifier");
        }
    }
}
