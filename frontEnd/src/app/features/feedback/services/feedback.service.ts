import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface CreateFeedbackRequest {
  title: string;
  description: string;
}

export interface FeedbackResponse {
  id: number;
  title: string;
  description: string;
  status: string;
}


/**
 * Feedback publié, sans données internes de modération.
 */
export interface PublicFeedback {
  id: number;
  title: string;
  description: string;
  applicationCode: string;
  createdAt: string;
}

/**
 * Page de feedbacks publiée par l'API.
 */
export interface PublicFeedbackPage {
  content: PublicFeedback[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}


export interface MyFeedback {
  id: number;
  title: string;
  description: string;
  applicationCode: string;
  authorId: number;
  createdAt: string;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
}

@Injectable({
  providedIn: 'root',
})
export class FeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/feedback';

  getMyVotes(): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/my-votes`);
  }

  support(id: number): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/vote`, null);
  }

  removeSupport(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}/vote`);
  }

  createFeedback(
    feedback: CreateFeedbackRequest,
  ): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(
      this.apiUrl,
      feedback,
    );
  }

  /**
   * Récupère les feedbacks approuvés avec pagination.
   * Le filtrage des statuts est assuré côté serveur.
   */
  getApprovedFeedbacks(
    page = 0,
    size = 10,
  ): Observable<PublicFeedbackPage> {
    if (!Number.isInteger(page) || page < 0) {
      throw new RangeError('Numéro de page invalide.');
    }

    if (!Number.isInteger(size) || size < 1 || size > 100) {
      throw new RangeError('Taille de page invalide.');
    }

    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PublicFeedbackPage>(
      this.apiUrl,
      { params },
    );
  }


  /**
   * Historique personnel : l'auteur est déterminé par le JWT côté API.
   */
  getMyFeedbacks(
    page = 0,
    size = 10,
  ): Observable<PublicFeedbackPage & { content: MyFeedback[] }> {
    if (!Number.isInteger(page) || page < 0) {
      throw new RangeError('Numéro de page invalide.');
    }

    if (!Number.isInteger(size) || size < 1 || size > 100) {
      throw new RangeError('Taille de page invalide.');
    }

    const params = new HttpParams()
      .set('page', page)
      .set('size', size);

    return this.http.get<PublicFeedbackPage & { content: MyFeedback[] }>(
      `${this.apiUrl}/mine`,
      { params },
    );
  }

}
