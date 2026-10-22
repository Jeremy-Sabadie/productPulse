import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import {
  FeedbackService,
  MyFeedback,
} from '../services/feedback.service';

interface HistoryPage {
  content: MyFeedback[];
  number: number;
  totalPages: number;
  totalElements: number;
  first: boolean;
  last: boolean;
}

@Component({
  selector: 'app-my-feedbacks',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './my-feedbacks.html',
  styleUrl: './my-feedbacks.css',
})
export class MyFeedbacks implements OnInit {
  private readonly service = inject(FeedbackService);

  readonly page = signal<HistoryPage | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.loadPage(0);
  }

  loadPage(pageNumber: number): void {
    if (this.loading() || pageNumber < 0) return;

    this.loading.set(true);
    this.error.set('');

    this.service.getMyFeedbacks(pageNumber, 10)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: response => this.page.set(response),
        error: () => this.error.set(
          'Impossible de charger votre historique.',
        ),
      });
  }

  previousPage(): void {
    const current = this.page();
    if (current && !current.first) {
      this.loadPage(current.number - 1);
    }
  }

  nextPage(): void {
    const current = this.page();
    if (current && !current.last) {
      this.loadPage(current.number + 1);
    }
  }

  statusLabel(status: MyFeedback['status']): string {
    switch (status) {
      case 'PENDING': return 'En attente';
      case 'APPROVED': return 'Approuvé';
      case 'REJECTED': return 'Refusé';
    }
  }
}
