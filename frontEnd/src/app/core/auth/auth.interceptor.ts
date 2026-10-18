import { inject } from '@angular/core';
import {
  HttpInterceptorFn,
} from '@angular/common/http';

import { AuthService } from './auth.service';

/**
 * Ajoute le JWT aux requêtes de l'API ProductPulse uniquement.
 */
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.getAccessToken();

  if (!token || !request.url.startsWith('/api/')) {
    return next(request);
  }

  const authenticatedRequest = request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`,
    },
  });

  return next(authenticatedRequest);
};
