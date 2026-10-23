import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  Feedback,
  FeedbackCategory,
  FeedbackPage,
  FeedbackStatus,
  GitHubIssueResult,
} from '../models/feedback.model';

/**
 * Critères de classement disponibles pour le dashboard.
 */
export type AdminFeedbackSort =
  | 'recent'
  | 'votes_desc'
  | 'votes_asc';

/**
 * Centralise les appels HTTP du dashboard administrateur.
 *
 * La vérification des droits reste assurée par Spring Security.
 */
@Injectable({
  providedIn: 'root',
})
export class AdminFeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/admin/feedback';

  /**
   * Récupère les feedbacks filtrés et paginés.
   * null signifie : tous les statuts.
   */
  getFeedbacks(
    page = 0,
    size = 10,
    status: FeedbackStatus | null = 'PENDING',
    applicationCode = '',
    sort: AdminFeedbackSort = 'recent',
    category: FeedbackCategory | '' = '',
  ): Observable<FeedbackPage> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', sort);

    if (status !== null) {
      params = params.set('status', status);
    }

    const application = applicationCode.trim();

    if (application) {
      params = params.set('applicationCode', application);
    }

    if (category) {
      params = params.set('category', category);
    }

    return this.http.get<FeedbackPage>(
      this.apiUrl,
      { params },
    );
  }

  /**
   * Récupère le nombre de soutiens d'un feedback.
   */
  getVoteCount(id: number): Observable<number> {
    return this.http.get<number>(
      `${this.apiUrl}/${id}/votes/count`,
    );
  }

  /**
   * Approuve un feedback.
   */
  approve(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/approve`,
      {},
    );
  }

  /**
   * Rejette un feedback.
   */
  reject(id: number): Observable<Feedback> {
    return this.http.patch<Feedback>(
      `${this.apiUrl}/${id}/reject`,
      {},
    );
  }

  /**
   * Crée une GitHub Issue à partir d'un feedback.
   */
  createGitHubIssue(id: number): Observable<GitHubIssueResult> {
    return this.http.post<GitHubIssueResult>(
      `${this.apiUrl}/${id}/github-issue`,
      {},
    );
  }
}
