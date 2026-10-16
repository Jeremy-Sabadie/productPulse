
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Feedback,
  FeedbackPage,
} from '../models/feedback.model';

/**
 * Centralise les échanges avec l'API d'administration ProductPulse.
 *
 * L'authentification sera intégrée séparément via un intercepteur HTTP,
 * lorsque le contrat d'authentification My Popote sera disponible.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminFeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin/feedback';

  /**
   * Récupère les feedbacks selon leur statut.
   *
   * @param page Numéro de page, à partir de zéro.
   * @param size Nombre d'éléments par page.
   * @param status Statut des feedbacks recherchés.
   */
  getFeedbacks(
    page = 0,
    size = 10,
    status = 'PENDING',
  ): Observable<FeedbackPage> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('status', status);

    return this.http.get<FeedbackPage>(this.apiUrl, { params });
  }

  /**
   * Approuve un feedback.
   *
   * @param id Identifiant du feedback.
   */
  approve(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/approve`,
      {},
    );
  }

  /**
   * Rejette un feedback.
   *
   * @param id Identifiant du feedback.
   */
  reject(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/reject`,
      {},
    );
  }
}
