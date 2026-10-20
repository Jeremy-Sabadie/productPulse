import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { FeedbackService } from '../services/feedback.service';

@Component({
  selector: 'app-feedback-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './feedback-form.html',
  styleUrl: './feedback-form.css',
})
export class FeedbackForm {
  private readonly feedbackService = inject(FeedbackService);

  title = '';
  description = '';

  readonly sending = signal(false);
  readonly success = signal(false);
  readonly error = signal('');

  submit(): void {
    const title = this.title.trim();
    const description = this.description.trim();

    if (!title || !description || this.sending()) {
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
          this.success.set(true);
          this.title = '';
          this.description = '';
        },
        error: () => {
          this.error.set(
            "Impossible d'envoyer votre avis. Veuillez réessayer.",
          );
        },
      });
  }
}
