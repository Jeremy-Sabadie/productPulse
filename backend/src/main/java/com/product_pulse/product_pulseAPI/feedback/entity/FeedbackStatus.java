
package com.product_pulse.product_pulseAPI.feedback.entity;

/**
 * Statuts de modération d'un retour utilisateur.
 *
 * Un nouveau feedback est toujours créé en attente
 * de validation par un administrateur.
 */
public enum FeedbackStatus {

    /**
     * Retour reçu, en attente de modération.
     */
    PENDING,

    /**
     * Retour approuvé et autorisé à être publié.
     */
    APPROVED,

    /**
     * Retour refusé par l'administrateur.
     */
    REJECTED
}
