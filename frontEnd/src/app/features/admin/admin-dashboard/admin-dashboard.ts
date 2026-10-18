import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';

import {
  Feedback,
  FeedbackPage,
  FeedbackStatus,
} from '../models/feedback.model';
import { AdminFeedbackService } from '../services/admin-feedback.service';

type FeedbackFilter = FeedbackStatus | null;

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

  readonly selectedStatus = signal<FeedbackFilter>('PENDING');

  readonly filters: {
    label: string;
    value: FeedbackFilter;
  }[] = [
    { label: 'Tous', value: null },
    { label: 'En attente', value: 'PENDING' },
    { label: 'Approuvés', value: 'APPROVED' },
    { label: 'Rejetés', value: 'REJECTED' },
  ];

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  selectStatus(status: FeedbackFilter): void {
    if (this.loading() || this.processingId() !== null) {
      return;
    }

    if (this.selectedStatus() === status) {
      return;
    }

    this.selectedStatus.set(status);
    this.loadFeedbacks(0);
  }

  loadFeedbacks(page = 0): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.actionError.set('');

    this.feedbackService
      .getFeedbacks(page, 10, this.selectedStatus())
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response: FeedbackPage) => {
          if (
            response.content.length === 0 &&
            response.totalElements > 0 &&
            page > 0
          ) {
            this.loading.set(false);
            this.loadFeedbacks(page - 1);
            return;
          }

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

  approve(id: number): void {
    this.processFeedback(id, 'approve');
  }

  reject(id: number): void {
    this.processFeedback(id, 'reject');
  }

  nextPage(): void {
    if (!this.loading() && this.currentPage() + 1 < this.totalPages()) {
      this.loadFeedbacks(this.currentPage() + 1);
    }
  }

  previousPage(): void {
    if (!this.loading() && this.currentPage() > 0) {
      this.loadFeedbacks(this.currentPage() - 1);
    }
  }

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