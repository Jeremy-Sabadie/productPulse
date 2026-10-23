
package com.product_pulse.product_pulseAPI.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Entité représentant un retour utilisateur ProductPulse.
 *
 * Un nouveau retour attend obligatoirement une décision
 * de modération avant sa publication.
 */
@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100, updatable = false)
    private String applicationCode;

    @Column(nullable = false, updatable = false)
    private Long authorId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackCategory category;

    @Column(name = "github_issue_number", unique = true)
    private Long githubIssueNumber;

    @Column(name = "github_issue_url", length = 500)
    private String githubIssueUrl;

    /**
     * Constructeur requis par JPA.
     */
    protected Feedback() {
    }

    /**
     * Crée un retour utilisateur en attente de modération.
     *
     * @param title titre du retour
     * @param description description du retour
     * @param applicationCode application concernée
     * @param authorId identifiant de l'auteur
     * @param createdAt date de création
     */
    public Feedback(
            String title,
            String description,
            String applicationCode,
            Long authorId,
            Instant createdAt
    ) {
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(
                description, "description"
        );
        this.applicationCode = Objects.requireNonNull(
                applicationCode, "applicationCode"
        );
        this.authorId = Objects.requireNonNull(authorId, "authorId");
        this.createdAt = Objects.requireNonNull(
                createdAt, "createdAt"
        );
        this.status = FeedbackStatus.PENDING;
        this.category = FeedbackCategory.IMPROVEMENT;
    }

    /**
     * Approuve un feedback encore en attente.
     *
     * @throws IllegalStateException si le feedback
     *         a déjà été modéré
     */
    /** Affecte la catégorie choisie lors de la création. */
    public void setCategory(FeedbackCategory category) {
        this.category = Objects.requireNonNull(category, "category");
    }

    public FeedbackCategory getCategory() {
        return category;
    }

    public void approve() {
        requirePending();
        status = FeedbackStatus.APPROVED;
    }

    /**
     * Refuse un feedback encore en attente.
     *
     * @throws IllegalStateException si le feedback
     *         a déjà été modéré
     */
    public void reject() {
        requirePending();
        status = FeedbackStatus.REJECTED;
    }

    private void requirePending() {
        if (status != FeedbackStatus.PENDING) {
            throw new IllegalStateException(
                    "Feedback has already been moderated"
            );
        }
    }

    public void linkGithubIssue(Long number, String url) {
        if (githubIssueNumber != null) {
            throw new IllegalStateException(
                    "Feedback already linked to a GitHub issue"
            );
        }

        if (number == null || number <= 0) {
            throw new IllegalArgumentException(
                    "GitHub issue number must be positive"
            );
        }

        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException(
                    "GitHub issue URL is required"
            );
        }

        this.githubIssueNumber = number;
        this.githubIssueUrl = url;
    }

    public Long getGithubIssueNumber() {
        return githubIssueNumber;
    }

    public String getGithubIssueUrl() {
        return githubIssueUrl;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getApplicationCode() {
        return applicationCode;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public FeedbackStatus getStatus() {
        return status;
    }
}
