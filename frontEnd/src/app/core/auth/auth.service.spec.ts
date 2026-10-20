import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthService);
  });

  afterEach(() => {
    sessionStorage.clear();
  });

  it('should start without an access token', () => {
    expect(service.getAccessToken()).toBeNull();
  });

  it('should retain an access token', () => {
    service.setAccessToken('test-token');

    expect(service.getAccessToken()).toBe('test-token');
  });

  it('should restore the token after a page reload', () => {
    service.setAccessToken('test-token');

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({});

    const restoredService = TestBed.inject(AuthService);

    expect(restoredService.getAccessToken()).toBe('test-token');
  });

  it('should clear the token from session storage', () => {
    service.setAccessToken('test-token');
    service.clearAccessToken();

    expect(service.getAccessToken()).toBeNull();
    expect(sessionStorage.getItem('productpulse.accessToken')).toBeNull();
  });
});
