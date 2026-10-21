import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { NotificationService } from '../../../core/services/notification.service';
import { FeedbackService } from '../services/feedback.service';

/**
 * Formulaire de contribution des utilisateurs My Popote.
 *
 * Les avis sont soumis à modération avant publication.
 * Les notifications sont centralisées dans NotificationService.
 */
@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './feedback-form.html',
  styleUrl: './feedback-form.css',
})
export class FeedbackForm {
  private readonly feedbackService = inject(FeedbackService);
  private readonly notifications = inject(NotificationService);

  title = '';
  description = '';

  readonly sending = signal(false);
  readonly success = signal(false);
  readonly error = signal('');

  /**
   * Valide et soumet le feedback.
   * Empêche les envois multiples pendant la requête.
   */
  async submit(): Promise<void> {
    const title = this.title.trim();
    const description = this.description.trim();

    if (!title || !description || this.sending()) {
      return;
    }

    const confirmed = await this.notifications.confirm(
      'Envoyer votre avis ?',
      'Votre contribution sera soumise à modération avant publication.',
    );

    if (!confirmed || this.sending()) {
      return;
    }

    this.sending.set(true);
    this.success.set(false);
    this.error.set('');

    this.feedbackService
      .createFeedback({ title, description })
      .pipe(finalize(() => this.sending.set(false)))
      .subscribe({
        next: () => {
          this.title = '';
          this.description = '';
          this.success.set(true);

          void this.notifications.feedbackSubmitted();
        },
        error: () => {
          const message =
            "Impossible d'envoyer votre avis. Veuillez réessayer.";

          this.error.set(message);
          void this.notifications.error(message);
        },
      });
  }
}
