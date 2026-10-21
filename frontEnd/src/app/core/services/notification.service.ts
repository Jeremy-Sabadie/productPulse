import { Injectable } from '@angular/core';
import Swal from 'sweetalert2';

/**
 * Centralise les notifications et confirmations de ProductPulse.
 * Les composants métier ne dépendent pas directement de SweetAlert2.
 */
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  /**
   * Confirme la bonne réception d'un feedback.
   */
  feedbackSubmitted(): Promise<unknown> {
    return Swal.fire({
      icon: 'success',
      title: 'Merci pour votre contribution !',
      text: 'Votre avis a bien été envoyé et sera publié après modération.',
      confirmButtonText: 'Compris',
      confirmButtonColor: '#4f46e5',
      customClass: {
        popup: 'productpulse-alert',
      },
    });
  }

  /**
   * Affiche une erreur sans exposer les détails techniques.
   */
  error(message: string): Promise<unknown> {
    return Swal.fire({
      icon: 'error',
      title: 'Une erreur est survenue',
      text: message,
      confirmButtonText: 'Fermer',
      confirmButtonColor: '#4f46e5',
    });
  }

  /**
   * Affiche une notification discrète, sans bloquer l'interface.
   */
  toast(message: string): Promise<unknown> {
    return Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: message,
      showConfirmButton: false,
      timer: 2500,
      timerProgressBar: true,
    });
  }

  /**
   * Demande confirmation avant une action sensible.
   */
  async confirm(
    title: string,
    message: string,
  ): Promise<boolean> {
    const result = await Swal.fire({
      icon: 'question',
      title,
      text: message,
      showCancelButton: true,
      confirmButtonText: 'Confirmer',
      cancelButtonText: 'Annuler',
      confirmButtonColor: '#4f46e5',
      cancelButtonColor: '#64748b',
      reverseButtons: true,
    });

    return result.isConfirmed;
  }
}
