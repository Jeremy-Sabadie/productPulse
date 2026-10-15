
package com.product_pulse.product_pulseAPI.feedback.repository;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository de persistance et de recherche des feedbacks.
 *
 * JpaRepository fournit les opérations CRUD et la pagination.
 * JpaSpecificationExecutor permet le filtrage dynamique.
 */
@Repository
public interface FeedbackRepository extends
        JpaRepository<Feedback, Long>,
        JpaSpecificationExecutor<Feedback> {
}
