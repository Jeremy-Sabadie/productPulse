package com.product_pulse.product_pulseAPI.feedback.repository;

import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackVote;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Accès aux votes des utilisateurs.
 */
public interface FeedbackVoteRepository extends JpaRepository<FeedbackVote, Long> {

    boolean existsByFeedbackIdAndUserId(Long feedbackId, Long userId);

    long deleteByFeedbackIdAndUserId(Long feedbackId, Long userId);

    long countByFeedbackId(Long feedbackId);
    /**
     * Retourne les identifiants des feedbacks soutenus par un utilisateur.
     */
    @Query("""
            SELECT v.feedback.id
            FROM FeedbackVote v
            WHERE v.userId = :userId
              AND v.feedback.status IN :statuses
            """)
    List<Long> findFeedbackIdsByUserIdAndStatusIn(
            @Param("userId") Long userId,
            @Param("statuses") java.util.Collection<FeedbackStatus> statuses
    );
}
