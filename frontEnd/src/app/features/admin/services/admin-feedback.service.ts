
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Feedback,
  FeedbackPage,
  FeedbackStatus,
} from '../models/feedback.model';

/**
 * Critères de classement disponibles pour le dashboard.
 *
 * Ces valeurs correspondent au contrat de l'API backend.
 * Elles ne doivent pas être modifiées indépendamment du Java.
 */
export type AdminFeedbackSort =
  | 'recent'
  | 'votes_desc'
  | 'votes_asc';

/**
 * Centralise les appels HTTP du dashboard administrateur.
 *
 * Responsabilités :
 * - récupérer les avis filtrés et paginés ;
 * - récupérer les compteurs de soutiens ;
 * - approuver ou rejeter les avis.
 *
 * Sécurité :
 * l'API Spring Security reste responsable de la vérification
 * des droits administrateur. Les filtres Angular ne sont
 * jamais une protection d'accès.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminFeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin/feedback';

  /**
   * Récupère une page d'avis avec les critères sélectionnés.
   *
   * Le filtrage et le classement sont réalisés côté backend,
   * avant la pagination.
   *
   * @param page Page demandée, à partir de zéro.
   * @param size Nombre d'avis par page.
   * @param status Statut de modération.
   * @param applicationCode Application à filtrer (facultatif).
   * @param sort Ordre de classement.
   */
  getFeedbacks(
    page = 0,
    size = 10,
    status: FeedbackStatus = 'PENDING',
    applicationCode = '',
    sort: AdminFeedbackSort = 'recent',
  ): Observable<FeedbackPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('status', status)
      .set('sort', sort);

    // Ne pas envoyer un filtre vide à l'API.
    const application = applicationCode.trim();

    if (application) {
      params = params.set('applicationCode', application);
    }

    return this.http.get<FeedbackPage>(
      this.apiUrl,
      { params },
    );
  }

  /**
   * Retourne le nombre de soutiens d'un avis.
   *
   * Ce compteur est réservé à l'administration.
   */
  getVoteCount(id: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${id}/votes/count`,
    );
  }

  /**
   * Approuve un avis en attente de modération.
   */
  approve(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/approve`,
      {},
    );
  }

  /**
   * Rejette un avis en attente de modération.
   */
  reject(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/reject`,
      {},
    );
  }
}
