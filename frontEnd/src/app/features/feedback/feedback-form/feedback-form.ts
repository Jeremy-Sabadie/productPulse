
import { Component, inject, signal } from '@angular/core';
import { FormsModule, NgModel } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { NotificationService } from '../../../core/services/notification.service';
import { FeedbackService } from '../services/feedback.service';

/**
 * Formulaire de contribution des utilisateurs My Popote.
 * Les avis sont soumis à modération avant publication.
 */
@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './feedback-form.html',
  styleUrl: './feedback-form.css',
})
export class FeedbackForm {
  private readonly feedbackService = inject(FeedbackService);
  private readonly notifications = inject(NotificationService);

  title = '';
  description = '';
  category: 'BUG' | 'IMPROVEMENT' | 'FEATURE' | '' = '';

  readonly sending = signal(false);
  readonly success = signal(false);
  readonly error = signal('');

  /**
   * Vérifie la validité du titre.
   * Retourne toujours un booléen.
   */
  titleInvalid(field: NgModel): boolean {
    return field.touched === true &&
      (field.invalid === true || !this.title.trim());
  }

  /**
   * Vérifie la validité de la description.
   * Retourne toujours un booléen.
   */
  descriptionInvalid(field: NgModel): boolean {
    return field.touched === true &&
      (field.invalid === true || !this.description.trim());
  }

  /**
   * Valide et soumet le feedback.
   * Empêche les envois multiples.
   */
  async submit(): Promise<void> {
    const title = this.title.trim();
    const description = this.description.trim();

    this.success.set(false);
    this.error.set('');

    if (!title || !description || !this.category) {
      this.error.set(
        'Complétez tous les champs avec des informations valides.',
      );
      return;
    }

    if (this.sending()) {
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

    this.feedbackService
      .createFeedback({
        title,
        description,
        category: this.category,
      })
      .pipe(finalize(() => this.sending.set(false)))
      .subscribe({
        next: () => {
          this.title = '';
          this.description = '';
          this.category = '';
          this.success.set(true);
        },
        error: () => {
          this.error.set(
            "Impossible d'envoyer votre avis. Veuillez réessayer.",
          );
        },
      });
  }
}
