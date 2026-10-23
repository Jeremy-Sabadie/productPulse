package com.product_pulse.product_pulseAPI.feedback.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Objects;

/**
 * Représente le soutien d'un utilisateur à un feedback.
 */
@Entity
@Table(name = "feedback_vote", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_feedback_vote_feedback_user",
                columnNames = {"feedback_id", "user_id"}
        )
})
public class FeedbackVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "feedback_id", nullable = false)
    private Feedback feedback;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FeedbackVote() {
        // Constructeur requis par JPA.
    }

    /**
     * Crée un vote pour un feedback.
     *
     * @param feedback feedback soutenu
     * @param userId identifiant de l'utilisateur
     */
    public FeedbackVote(Feedback feedback, Long userId) {
        this.feedback = Objects.requireNonNull(feedback);
        this.userId = Objects.requireNonNull(userId);
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
