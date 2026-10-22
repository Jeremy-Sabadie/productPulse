import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-auth-page',
  standalone: true,
  template: `
    <main style="max-width:480px;margin:80px auto;padding:24px">
      <h1>ProductPulse</h1>

      <p>{{ message }}</p>

      @if (!completed) {
        <button
          type="button"
          (click)="reconnect()"
          style="
            padding:12px 20px;
            border:0;
            border-radius:8px;
            background:#4f46e5;
            color:white;
            cursor:pointer;
            font-size:15px;
          "
        >
          Se connecter avec My Popote
        </button>
      }
    </main>
  `,
})
export class AuthPage implements OnInit, OnDestroy {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);

  private readonly myPopoteOrigin = 'http://localhost:4201';
  private nonce: string | null = null;

  completed = false;
  message = 'Connectez-vous avec My Popote pour accéder à ProductPulse.';

  private readonly onMessage = (event: MessageEvent): void => {
    if (event.origin !== this.myPopoteOrigin) return;
    if (event.source !== window.opener) return;
    if (this.completed) return;

    if (
      event.data?.type !== 'PRODUCTPULSE_AUTH_RESPONSE' ||
      event.data?.nonce !== this.nonce ||
      typeof event.data?.accessToken !== 'string'
    ) {
      return;
    }

    this.completed = true;
    this.message = 'Vérification de vos droits...';
    this.auth.setAccessToken(event.data.accessToken);

    this.http.get<{ role: 'ADMIN' | 'USER' }>('/api/auth/me')
      .subscribe({
        next: (response) => {
          const requestedUrl = this.route.snapshot.queryParamMap.get('returnUrl');
          const fallback = response.role === 'ADMIN'
            ? '/admin/feedback'
            : '/feedback/new';

          const destination =
            requestedUrl?.startsWith('/') &&
            !requestedUrl.startsWith('//') &&
            !requestedUrl.startsWith('/auth')
              ? requestedUrl
              : fallback;

          void this.router.navigateByUrl(destination);
        },
        error: () => {
          this.auth.clearAccessToken();
          this.completed = false;
          this.message = 'Connexion impossible. Veuillez réessayer.';
        },
      });
  };

  ngOnInit(): void {
    if (!window.opener) {
      return;
    }

    window.addEventListener('message', this.onMessage);

    const bytes = crypto.getRandomValues(new Uint8Array(16));

    this.nonce = Array.from(bytes, (byte) =>
      byte.toString(16).padStart(2, '0')
    ).join('');

    window.opener.postMessage(
      {
        type: 'PRODUCTPULSE_AUTH_REQUEST',
        nonce: this.nonce,
      },
      this.myPopoteOrigin,
    );
  }

  reconnect(): void {
    window.location.href = `${this.myPopoteOrigin}/productpulse-auth`;
  }

  ngOnDestroy(): void {
    window.removeEventListener('message', this.onMessage);
  }
}
