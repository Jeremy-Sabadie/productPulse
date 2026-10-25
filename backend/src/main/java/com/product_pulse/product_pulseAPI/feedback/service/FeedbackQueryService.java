
package com.product_pulse.product_pulseAPI.feedback.service;

import com.product_pulse.product_pulseAPI.feedback.entity.Feedback;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackStatus;
import com.product_pulse.product_pulseAPI.feedback.entity.FeedbackCategory;
import com.product_pulse.product_pulseAPI.feedback.repository.FeedbackRepository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service de consultation des avis.
 *
 * Centralise :
 * - les recherches administrateur ;
 * - les filtres et le classement ;
 * - la pagination ;
 * - l'historique personnel des utilisateurs.
 *
 * Ce service ne réalise aucune opération de modération.
 */
@Service
public class FeedbackQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final FeedbackRepository repository;

    public FeedbackQueryService(FeedbackRepository repository) {
        this.repository = Objects.requireNonNull(
                repository, "repository"
        );
    }

    /**
     * Compatibilité avec les appels existants.
     *
     * Important : cette surcharge évite de casser les autres
     * composants et tests qui utilisent déjà cette signature.
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findAll(
            FeedbackStatus status,
            int page,
            int size
    ) {
        return findAll(status, null, null, "recent", page, size);
    }

    /**
     * Recherche administrative avec filtres et classement.
     *
     * @param status statut de modération facultatif
     * @param applicationCode code de l'application facultatif
     * @param sort recent, votes_desc ou votes_asc
     * @param page numéro de page commençant à zéro
     * @param size taille de page
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findAll(
            FeedbackStatus status,
            String applicationCode,
            String sort,
            int page,
            int size
    ) {
        return findAll(status, applicationCode, null, sort, page, size);
    }

    /** Recherche combinant statut, application, catégorie et tri en base. */
    @Transactional(readOnly = true)
    public Page<Feedback> findAll(
            FeedbackStatus status,
            String applicationCode,
            FeedbackCategory category,
            String sort,
            int page,
            int size
    ) {
        validatePagination(page, size);

        String application = applicationCode == null
                || applicationCode.isBlank()
                ? null
                : applicationCode.trim();

        String ordering = sort == null ? "recent" : sort;

        if (!List.of("recent", "votes_desc", "votes_asc")
                .contains(ordering)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Critère de tri invalide"
            );
        }

        /*
         * Pour les classements par soutiens, le repository
         * effectue l'agrégation avant la pagination.
         *
         * On n'ajoute pas de tri Spring Data concurrent :
         * la requête JPQL définit déjà l'ordre complet.
         */
        if (!ordering.equals("recent")) {
            Pageable pageable = PageRequest.of(page, size);

            return ordering.equals("votes_desc")
                    ? repository.findSortedByVotesDesc(
                            status, application, category, pageable)
                    : repository.findSortedByVotesAsc(
                            status, application, category, pageable);
        }

        /*
         * Pour le tri chronologique, les filtres facultatifs
         * sont composés sans multiplier les méthodes repository.
         */
        Specification<Feedback> specification =
                (root, query, builder) -> {
                    List<Predicate> predicates = new ArrayList<>();

                    if (status != null) {
                        predicates.add(builder.equal(
                                root.get("status"), status
                        ));
                    }

                    if (category != null) {
                        predicates.add(builder.equal(root.get("category"), category));
                    }

                    if (application != null) {
                        predicates.add(builder.equal(
                                root.get("applicationCode"),
                                application
                        ));
                    }

                    return builder.and(
                            predicates.toArray(new Predicate[0])
                    );
                };

        return repository.findAll(
                specification,
                createPageable(page, size)
        );
    }

    @Transactional(readOnly = true)
    public Page<Feedback> findCommunity(int page, int size) {
        return repository.findByStatusIn(
                List.of(FeedbackStatus.PENDING, FeedbackStatus.APPROVED),
                createPageable(page, size)
        );
    }

    /**
     * Historique personnel : US07.
     *
     * La sélection se fait obligatoirement avec l'identifiant
     * de l'utilisateur authentifié transmis par le contrôleur.
     *
     * Ne jamais remplacer cet identifiant par une valeur
     * librement fournie depuis l'interface utilisateur.
     */
    @Transactional(readOnly = true)
    public Page<Feedback> findMine(
            Long authorId,
            int page,
            int size
    ) {
        Objects.requireNonNull(authorId, "authorId");

        Specification<Feedback> specification =
                (root, query, builder) ->
                        builder.equal(root.get("authorId"), authorId);

        return repository.findAll(
                specification,
                createPageable(page, size)
        );
    }

    /**
     * Construit une pagination avec un tri chronologique stable.
     *
     * L'identifiant sert de second critère lorsque plusieurs
     * avis ont exactement le même horodatage.
     */
    private Pageable createPageable(int page, int size) {
        validatePagination(page, size);

        return PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "id"))
        );
    }

    /**
     * Protège l'API contre les tailles de page invalides
     * ou excessives.
     */
    private void validatePagination(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Paramètres de pagination invalides"
            );
        }
    }
}
