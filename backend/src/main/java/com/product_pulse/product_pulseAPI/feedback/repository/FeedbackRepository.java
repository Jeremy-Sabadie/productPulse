
package com.product_pulse.product_pulseAPI.feedback.repository;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository Spring Data JPA chargé de la persistance
 * des feedbacks.
 *
 * <p>Les opérations CRUD standards sont fournies par
 * JpaRepository. Aucune requête personnalisée n'est
 * nécessaire pour le moment.</p>
 */
public interface FeedbackRepository
        extends JpaRepository<Feedback, Long> {
}
