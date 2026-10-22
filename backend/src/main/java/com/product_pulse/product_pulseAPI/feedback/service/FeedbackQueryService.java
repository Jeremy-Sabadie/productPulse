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

import java.util.Objects;

/**
 * Service de consultation des feedbacks.
 *
 * Gère les recherches administrateur et l'historique
 * personnel des utilisateurs.
 */
@Service
public class FeedbackQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FeedbackRepository repository;

    public FeedbackQueryService(FeedbackRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    /**
     * Recherche les feedbacks pour l'administration,
     * avec un filtre optionnel sur le statut.
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findAll(
            FeedbackStatus status,
            int page,
            int size
    ) {
        Pageable pageable = createPageable(page, size);

        if (status == null) {
            return repository.findAll(pageable);
        }

        Specification<Feedback> specification =
                (root, query, builder) ->
                        builder.equal(root.get("status"), status);

        return repository.findAll(specification, pageable);
    }

    /**
     * Recherche exclusivement les feedbacks d'un auteur.
     *
     * Tous les statuts sont inclus dans l'historique.
     *
     * @param authorId identifiant issu du JWT authentifié
     * @param page numéro de page à partir de zéro
     * @param size nombre de feedbacks par page
     * @return page des feedbacks de l'auteur
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findMine(
            Long authorId,
            int page,
            int size
    ) {
        Objects.requireNonNull(authorId, "authorId");

        Pageable pageable = createPageable(page, size);

        Specification<Feedback> specification =
                (root, query, builder) ->
                        builder.equal(root.get("authorId"), authorId);

        return repository.findAll(specification, pageable);
    }

    /**
     * Construit une pagination avec un tri stable.
     */
    private Pageable createPageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Paramètres de pagination invalides"
            );
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }
}
