
package com.product_pulse.product_pulseAPI.feedback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

/**
 * Entité représentant un feedback enregistré dans ProductPulse.
 *
 * <p>Chaque feedback appartient à une application et possède
 * un auteur identifié.</p>
 *
 * <p>L'entité ne propose pas de setters publics.
 * La date de création est fournie par le service métier
 * pour garantir des tests déterministes.</p>
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

    /**
     * Constructeur requis par JPA.
     */
    protected Feedback() {
    }

    /**
     * Construit un feedback avec ses informations obligatoires.
     *
     * @param title titre du feedback
     * @param description description du feedback
     * @param applicationCode code de l'application concernée
     * @param authorId identifiant de l'auteur
     * @param createdAt date de création
     * @throws NullPointerException si un argument est null
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
}
