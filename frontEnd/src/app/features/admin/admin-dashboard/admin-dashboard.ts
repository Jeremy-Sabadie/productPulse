
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { finalize } from 'rxjs';

import {
  Feedback,
  FeedbackCategory,
  FeedbackPage,
  FeedbackStatus,
} from '../models/feedback.model';

import {
  AdminFeedbackService,
  AdminFeedbackSort,
} from '../services/admin-feedback.service';

/**
 * Dashboard administrateur ProductPulse.
 *
 * US08 :
 * - consulter les avis ;
 * - filtrer par application et statut ;
 * - approuver ou rejeter les avis.
 *
 * US14 :
 * - classer les avis par nombre de soutiens ;
 * - réserver les compteurs à l'administration.
 *
 * - filtrer les avis par catégorie métier.
 */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [DatePipe],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit {
  private readonly feedbackService = inject(AdminFeedbackService);

  /** Avis affichés sur la page courante. */
  readonly feedbacks = signal<Feedback[]>([]);

  /** Critères de recherche administrateur. */
  readonly view = signal<FeedbackStatus>('PENDING');
  readonly applicationCode = signal('');
  readonly category = signal<FeedbackCategory | ''>('');
  readonly sort = signal<AdminFeedbackSort>('recent');

  /** Compteurs visibles uniquement dans le dashboard admin. */
  readonly voteCounts = signal<Record<number, number>>({});

  /** États d'interface. */
  readonly loading = signal(false);
  readonly error = signal('');
  readonly actionError = signal('');
  readonly processingId = signal<number | null>(null);

  /** Pagination retournée par Spring Data. */
  readonly currentPage = signal(0);
  readonly totalPages = signal(0);
  readonly totalElements = signal<number | null>(null);

  ngOnInit(): void {
    this.loadFeedbacks();
  }

  /**
   * Charge les avis selon les critères actifs.
   *
   * Le backend réalise les filtres et le classement AVANT
   * la pagination. Le frontend ne trie donc jamais les
   * dix résultats localement.
   */
  loadFeedbacks(page = 0): void {
    if (this.loading()) {
      return;
    }

    this.loading.set(true);
    this.error.set('');
    this.actionError.set('');

    this.feedbackService
      .getFeedbacks(
        page,
        10,
        this.view(),
        this.applicationCode(),
        this.sort(),
        this.category(),
      )
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response: FeedbackPage) => {
          this.feedbacks.set(response.content);
          this.currentPage.set(response.number);
          this.totalPages.set(response.totalPages);
          this.totalElements.set(response.totalElements);

          // Réinitialise les compteurs de la page précédente.
          this.voteCounts.set({});

          /*
           * Compatibilité avec l'API actuelle :
           * le compteur est récupéré séparément pour chaque avis.
           *
           * Optimisation ultérieure :
           * inclure voteCount dans le DTO administrateur
           * pour supprimer ces requêtes supplémentaires.
           */
          if (this.view() === 'APPROVED') {
            for (const feedback of response.content) {
              this.feedbackService
                .getVoteCount(feedback.id)
                .subscribe({
                  next: count => {
                    this.voteCounts.update(counts => ({
                      ...counts,
                      [feedback.id]: count,
                    }));
                  },
                  error: () => {
                    this.actionError.set(
                      'Impossible de charger certains compteurs.',
                    );
                  },
                });
            }
          }
        },
        error: () => {
          this.feedbacks.set([]);
          this.voteCounts.set({});
          this.totalElements.set(null);
          this.totalPages.set(0);

          this.error.set(
            'Impossible de charger les feedbacks.',
          );
        },
      });
  }

  /**
   * Change le statut de modération affiché.
   *
   * Un changement de filtre ramène toujours
   * à la première page.
   */
  changeView(view: FeedbackStatus): void {
    if (this.loading() || this.view() === view) {
      return;
    }

    this.view.set(view);
    this.loadFeedbacks(0);
  }

  /**
   * Filtre les avis par code d'application.
   *
   * Le champ utilise l'événement change plutôt que input :
   * cela évite une requête HTTP à chaque caractère saisi.
   */
  changeApplication(value: string): void {
    const application = value.trim();

    if (
      this.loading() ||
      this.applicationCode() === application
    ) {
      return;
    }

    this.applicationCode.set(application);
    this.loadFeedbacks(0);
  }

  /** Filtre par catégorie métier et revient à la première page. */
  changeCategory(value: string): void {
    if (value !== '' && value !== 'BUG' && value !== 'IMPROVEMENT' && value !== 'FEATURE') {
      return;
    }
    if (this.loading() || this.category() === value) {
      return;
    }
    this.category.set(value as FeedbackCategory | '');
    this.loadFeedbacks(0);
  }

  /**
   * Change l'ordre de classement.
   *
   * Les valeurs acceptées sont volontairement limitées
   * au contrat défini dans AdminFeedbackSort.
   */
  changeSort(value: string): void {
    if (
      value !== 'recent' &&
      value !== 'votes_desc' &&
      value !== 'votes_asc'
    ) {
      return;
    }

    if (this.loading() || this.sort() === value) {
      return;
    }

    this.sort.set(value);
    this.loadFeedbacks(0);
  }

  /** Approuve un avis et recharge la liste. */
  approve(id: number): void {
    this.processFeedback(id, 'approve');
  }

  /** Rejette un avis et recharge la liste. */
  reject(id: number): void {
    this.processFeedback(id, 'reject');
  }

  /**
   * Crée une issue GitHub à partir d'un feedback.
   *
   * Empêche les créations multiples pendant le traitement.
   * La confirmation évite toute création accidentelle.
   */
  createGitHubIssue(feedback: Feedback): void {
    if (
      this.processingId() !== null ||
      this.loading() ||
      feedback.githubIssueNumber !== null
    ) {
      return;
    }

    if (!window.confirm(
      `Créer une issue GitHub pour le feedback #${feedback.id} ?`
    )) {
      return;
    }

    this.processingId.set(feedback.id);
    this.actionError.set('');

    this.feedbackService.createGitHubIssue(feedback.id)
      .pipe(finalize(() => this.processingId.set(null)))
      .subscribe({
        next: issue => {
          this.feedbacks.update(feedbacks =>
            feedbacks.map(item =>
              item.id === feedback.id
                ? {
                    ...item,
                    githubIssueNumber: issue.number,
                    githubIssueUrl: issue.url,
                  }
                : item
            )
          );
        },
        error: () => {
          this.actionError.set(
            'Impossible de créer l’issue GitHub. Vérifiez son existence sur GitHub avant de réessayer.'
          );
        },
      });
  }

  /** Navigation vers la page suivante. */
  nextPage(): void {
    if (
      !this.loading() &&
      this.currentPage() + 1 < this.totalPages()
    ) {
      this.loadFeedbacks(this.currentPage() + 1);
    }
  }

  /** Navigation vers la page précédente. */
  previousPage(): void {
    if (!this.loading() && this.currentPage() > 0) {
      this.loadFeedbacks(this.currentPage() - 1);
    }
  }

  /**
   * Exécute une action de modération.
   *
   * Une seule action peut être en cours à la fois.
   * L'avis est ensuite rechargé depuis l'API :
   * on ne simule pas sa nouvelle valeur localement.
   */
  private processFeedback(
    id: number,
    action: 'approve' | 'reject',
  ): void {
    if (
      this.processingId() !== null ||
      this.loading()
    ) {
      return;
    }

    this.processingId.set(id);
    this.actionError.set('');

    const request = action === 'approve'
      ? this.feedbackService.approve(id)
      : this.feedbackService.reject(id);

    request
      .pipe(finalize(() => this.processingId.set(null)))
      .subscribe({
        next: () => {
          this.loadFeedbacks(this.currentPage());
        },
        error: () => {
          this.actionError.set(
            'Impossible de modifier ce feedback. Veuillez réessayer.',
          );
        },
      });
  }
}
