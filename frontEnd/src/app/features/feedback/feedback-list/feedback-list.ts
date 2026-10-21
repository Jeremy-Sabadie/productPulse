import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import {
  FeedbackService,
  PublicFeedbackPage,
} from '../services/feedback.service';

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './feedback-list.html',
  styleUrl: './feedback-list.css',
})
export class FeedbackList implements OnInit {
  private readonly service = inject(FeedbackService);

  readonly page = signal<PublicFeedbackPage | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly pageSize = 10;

  ngOnInit(): void {
    this.loadPage(0);
  }

  /**
   * Charge une page de feedbacks approuvés.
   * Conserve la page courante en cas d'erreur réseau.
   */
  loadPage(pageNumber: number): void {
    if (this.loading() || pageNumber < 0) {
      return;
    }

    this.loading.set(true);
    this.error.set('');

    this.service.getApprovedFeedbacks(pageNumber, this.pageSize)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => this.page.set(response),
        error: () => this.error.set(
          'Impossible de charger les avis. Veuillez réessayer.',
        ),
      });
  }

  nextPage(): void {
    const current = this.page();
    if (current && !current.last) {
      this.loadPage(current.number + 1);
    }
  }

  previousPage(): void {
    const current = this.page();
    if (current && !current.first) {
      this.loadPage(current.number - 1);
    }
  }
}
