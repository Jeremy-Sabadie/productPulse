
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service de consultation des feedbacks pour l'administration.
 */
@Service
public class FeedbackQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FeedbackRepository repository;

    public FeedbackQueryService(FeedbackRepository repository) {
        this.repository = repository;
    }

    /**
     * Recherche les feedbacks avec pagination et filtre optionnel.
     *
     * @param status statut à filtrer, ou null pour tous
     * @param page numéro de page à partir de zéro
     * @param size nombre d'éléments par page
     * @return page de feedbacks triés par date décroissante
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findAll(
            FeedbackStatus status,
            int page,
            int size
    ) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Paramètres de pagination invalides"
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                ).and(Sort.by(Sort.Direction.DESC, "id"))
        );

        if (status == null) {
            return repository.findAll(pageable);
        }

        Specification<Feedback> specification =
                (root, query, builder) ->
                        builder.equal(root.get("status"), status);

        return repository.findAll(specification, pageable);
    }
}
