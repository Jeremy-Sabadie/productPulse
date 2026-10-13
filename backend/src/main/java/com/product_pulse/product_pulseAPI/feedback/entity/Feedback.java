
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
 * <p>L'entité protège son état en ne proposant pas
 * de setters publics.</p>
 *
 * <p>La date de création est fournie par le service métier
 * afin de permettre des tests déterministes.</p>
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
     * @param createdAt date de création
     * @throws NullPointerException si un argument est null
     */
    public Feedback(
            String title,
            String description,
            Instant createdAt
    ) {
        this.title = Objects.requireNonNull(title, "title");
        this.description = Objects.requireNonNull(
                description, "description"
        );
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
