import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import {
  FeedbackService,
  PublicFeedbackPage,
} from '../services/feedback.service';

@Component({
  selector: 'app-feedback-list',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './feedback-list.html',
  styleUrl: './feedback-list.css',
})
export class FeedbackList implements OnInit {
  private readonly service = inject(FeedbackService);

  readonly page = signal<PublicFeedbackPage | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');
  readonly myVotes = signal<Set<number>>(new Set());
  readonly voting = signal<Set<number>>(new Set());
  readonly votesReady = signal(false);
  readonly pageSize = 10;

  ngOnInit(): void {
    this.loadPage(0);
    this.service.getMyVotes().subscribe({
      next: ids => {
        this.myVotes.set(new Set(ids));
        this.votesReady.set(true);
      },
      error: () => this.error.set(
        'Impossible de charger vos soutiens.',
      ),
    });
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


  toggleSupport(id: number): void {
    if (!this.votesReady() || this.voting().has(id)) return;

    const alreadySupported = this.myVotes().has(id);
    this.voting.update(ids => new Set([...ids, id]));
    this.error.set('');

    const request = alreadySupported
      ? this.service.removeSupport(id)
      : this.service.support(id);

    request.pipe(finalize(() => {
      this.voting.update(ids => {
        const next = new Set(ids);
        next.delete(id);
        return next;
      });
    })).subscribe({
      next: () => this.myVotes.update(ids => {
        const next = new Set(ids);
        if (alreadySupported) next.delete(id);
        else next.add(id);
        return next;
      }),
      error: () => this.error.set(
        'Impossible de modifier votre soutien.',
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
