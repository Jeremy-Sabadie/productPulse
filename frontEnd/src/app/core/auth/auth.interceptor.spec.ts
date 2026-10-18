import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { AuthService } from './auth.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpMock: HttpTestingController;
  let auth: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });

    http = TestBed.inject(HttpClient);
    httpMock = TestBed.inject(HttpTestingController);
    auth = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('adds the JWT to ProductPulse API requests', () => {
    auth.setAccessToken('test-token');

    http.get('/api/admin/feedback').subscribe();

    const request = httpMock.expectOne('/api/admin/feedback');

    expect(request.request.headers.get('Authorization'))
      .toBe('Bearer test-token');

    request.flush({});
  });

  it('does not add Authorization when no token exists', () => {
    http.get('/api/admin/feedback').subscribe();

    const request = httpMock.expectOne('/api/admin/feedback');

    expect(request.request.headers.has('Authorization')).toBe(false);

    request.flush({});
  });

  it('does not send the JWT to unrelated URLs', () => {
    auth.setAccessToken('test-token');

    http.get('https://example.com/data').subscribe();

    const request = httpMock.expectOne('https://example.com/data');

    expect(request.request.headers.has('Authorization')).toBe(false);

    request.flush({});
  });
});
