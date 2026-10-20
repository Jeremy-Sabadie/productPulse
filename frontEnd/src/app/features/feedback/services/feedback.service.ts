import { HttpClient } from '@angular/common/http';
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

@Injectable({
  providedIn: 'root',
})
export class FeedbackService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = '/api/feedback';

  createFeedback(
    feedback: CreateFeedbackRequest,
  ): Observable<FeedbackResponse> {
    return this.http.post<FeedbackResponse>(
      this.apiUrl,
      feedback,
    );
  }
}
