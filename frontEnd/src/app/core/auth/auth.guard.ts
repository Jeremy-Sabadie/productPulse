import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';

import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.getAccessToken()) {
    return true;
  }

  return router.createUrlTree(['/auth'], {
    queryParams: { returnUrl: state.url },
  });
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const http = inject(HttpClient);

  if (!auth.getAccessToken()) {
    return router.createUrlTree(['/auth']);
  }

  return http.get<{ role: 'ADMIN' | 'USER' }>('/api/auth/me').pipe(
    map((response) =>
      response.role === 'ADMIN'
        ? true
        : router.createUrlTree(['/feedback/new']),
    ),
    catchError(() => of(router.createUrlTree(['/auth']))),
  );
};
