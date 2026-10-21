import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { Router, UrlTree, provideRouter } from '@angular/router';
import { firstValueFrom, isObservable, Observable } from 'rxjs';

import { AuthService } from './auth.service';
import { adminGuard } from './auth.guard';

describe('adminGuard', () => {
  let auth: AuthService;
  let httpMock: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    sessionStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    auth = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpMock.verify();
    sessionStorage.clear();
  });

  async function checkAdminAccess(role: 'ADMIN' | 'USER') {
    auth.setAccessToken('test-token');

    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, {} as never),
    );

    const pending = isObservable(result)
      ? firstValueFrom(result as Observable<boolean | UrlTree>)
      : Promise.resolve(result as boolean | UrlTree);

    const request = httpMock.expectOne('/api/auth/me');
    expect(request.request.method).toBe('GET');
    request.flush({ role });

    return pending;
  }

  it('allows an administrator', async () => {
    expect(await checkAdminAccess('ADMIN')).toBe(true);
  });

  it('redirects a regular user to the feedback form', async () => {
    const result = await checkAdminAccess('USER');

    expect(result instanceof UrlTree).toBe(true);
    expect(router.serializeUrl(result as UrlTree))
      .toBe('/feedback/new');
  });

  it('redirects to authentication when the token is absent', async () => {
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as never, {} as never),
    );

    const resolved = isObservable(result)
      ? await firstValueFrom(result as Observable<boolean | UrlTree>)
      : await Promise.resolve(result as boolean | UrlTree);

    expect(router.serializeUrl(resolved as UrlTree)).toBe('/auth');
    httpMock.expectNone('/api/auth/me');
  });
});
