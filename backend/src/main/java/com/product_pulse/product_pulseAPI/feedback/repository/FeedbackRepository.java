
package com.product_pulse.product_pulseAPI.feedback.repository;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Accès aux données des avis ProductPulse.
 *
 * Responsabilités :
 * - CRUD et pagination via JpaRepository ;
 * - filtres dynamiques via JpaSpecificationExecutor ;
 * - classement des avis selon leur nombre de soutiens.
 *
 * Le classement doit être exécuté en base de données AVANT
 * la pagination pour garantir un ordre correct entre les pages.
 */
@Repository
public interface FeedbackRepository extends
        JpaRepository<Feedback, Long>,
        JpaSpecificationExecutor<Feedback> {

    Page<Feedback> findByStatusIn(
            java.util.Collection<FeedbackStatus> statuses,
            Pageable pageable
    );

    /**
     * Recherche les avis classés du plus soutenu au moins soutenu.
     *
     * LEFT JOIN : conserve les avis qui n'ont aucun soutien.
     * countQuery : permet à Spring Data de calculer le nombre
     * total de résultats pour la pagination.
     *
     * Les critères de filtrage sont facultatifs.
     */
    @Query(value = """
        SELECT f
        FROM Feedback f
        LEFT JOIN FeedbackVote v ON v.feedback = f
        WHERE (:status IS NULL OR f.status = :status)
          AND (:applicationCode IS NULL
               OR f.applicationCode = :applicationCode)
           AND (:category IS NULL OR f.category = :category)
        GROUP BY f
        ORDER BY COUNT(v.id) DESC, f.createdAt DESC, f.id DESC
        """, countQuery = """
        SELECT COUNT(f)
        FROM Feedback f
        WHERE (:status IS NULL OR f.status = :status)
          AND (:applicationCode IS NULL
               OR f.applicationCode = :applicationCode)
           AND (:category IS NULL OR f.category = :category)
        """)
    Page<Feedback> findSortedByVotesDesc(
            @Param("status") FeedbackStatus status,
            @Param("applicationCode") String applicationCode,
            @Param("category") FeedbackCategory category,
            Pageable pageable
    );

    /**
     * Recherche les avis classés du moins soutenu au plus soutenu.
     *
     * Les avis ayant zéro soutien apparaissent en premier.
     * L'horodatage et l'identifiant départagent les égalités.
     */
    @Query(value = """
        SELECT f
        FROM Feedback f
        LEFT JOIN FeedbackVote v ON v.feedback = f
        WHERE (:status IS NULL OR f.status = :status)
          AND (:applicationCode IS NULL
               OR f.applicationCode = :applicationCode)
           AND (:category IS NULL OR f.category = :category)
        GROUP BY f
        ORDER BY COUNT(v.id) ASC, f.createdAt DESC, f.id DESC
        """, countQuery = """
        SELECT COUNT(f)
        FROM Feedback f
        WHERE (:status IS NULL OR f.status = :status)
          AND (:applicationCode IS NULL
               OR f.applicationCode = :applicationCode)
           AND (:category IS NULL OR f.category = :category)
        """)
    Page<Feedback> findSortedByVotesAsc(
            @Param("status") FeedbackStatus status,
            @Param("applicationCode") String applicationCode,
            @Param("category") FeedbackCategory category,
            Pageable pageable
    );
}
