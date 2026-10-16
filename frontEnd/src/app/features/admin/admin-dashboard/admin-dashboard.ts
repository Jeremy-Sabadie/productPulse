
import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

import { Feedback, FeedbackPage } from '../models/feedback.model';
import { AdminFeedbackService } from '../services/admin-feedback.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  private readonly feedbackService = inject(AdminFeedbackService);

  readonly feedbacks = signal<Feedback[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly actionError = signal('');
  readonly processingId = signal<number | null>(null);
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal<number | null>(null);

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  /**
   * Charge une page de feedbacks en attente.
   *
   * @param page Numéro de page, à partir de zéro.
   */
  loadFeedbacks(page = 0): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.actionError.set('');

    this.feedbackService
      .getFeedbacks(page, 10, 'PENDING')
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response: FeedbackPage) => {
          this.feedbacks.set(response.content);
          this.currentPage.set(response.number);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);
        },
        error: () => {
          this.feedbacks.set([]);
          this.totalElements.set(null);
          this.totalPages.set(0);
          this.error.set('Impossible de charger les feedbacks.');
        },
      });
  }

  /**
   * Approuve un feedback et actualise la liste.
   *
   * @param id Identifiant du feedback.
   */
  approve(id: number): void {
    this.processFeedback(id, 'approve');
  }

  /**
   * Rejette un feedback et actualise la liste.
   *
   * @param id Identifiant du feedback.
   */
  reject(id: number): void {
    this.processFeedback(id, 'reject');
  }

  /**
   * Charge la page suivante lorsqu'elle existe.
   */
  nextPage(): void {
    if (!this.loading() && this.currentPage() + 1 < this.totalPages()) {
      this.loadFeedbacks(this.currentPage() + 1);
    }
  }

  /**
   * Charge la page précédente lorsqu'elle existe.
   */
  previousPage(): void {
    if (!this.loading() && this.currentPage() > 0) {
      this.loadFeedbacks(this.currentPage() - 1);
    }
  }

  /**
   * Exécute une action de modération.
   */
  private processFeedback(
    id: number,
    action: 'approve' | 'reject',
  ): void {
    if (this.processingId() !== null || this.loading()) {
      return;
    }

    this.processingId.set(id);
    this.actionError.set('');

    const request =
      action === 'approve'
        ? this.feedbackService.approve(id)
        : this.feedbackService.reject(id);

    request
      .pipe(finalize(() => this.processingId.set(null)))
      .subscribe({
        next: () => this.loadFeedbacks(this.currentPage()),
        error: () => {
          this.actionError.set(
            'Impossible de modifier ce feedback. Veuillez réessayer.',
          );
        },
      });
  }
}
