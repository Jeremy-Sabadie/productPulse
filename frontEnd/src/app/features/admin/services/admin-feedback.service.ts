import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Feedback,
  FeedbackPage,
  FeedbackStatus,
} from '../models/feedback.model';

/**
 * Centralise les échanges avec l'API d'administration ProductPulse.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminFeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin/feedback';

  /**
   * Récupère les feedbacks paginés avec un filtre optionnel.
   * null signifie : tous les statuts.
   */
  getFeedbacks(
    page = 0,
    size = 10,
    status: FeedbackStatus | null = 'PENDING',
  ): Observable<FeedbackPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status !== null) {
      params = params.set('status', status);
    }

    return this.http.get<FeedbackPage>(this.apiUrl, { params });
  }

  approve(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/approve`,
      {},
    );
  }

  reject(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/reject`,
      {},
    );
  }
}
