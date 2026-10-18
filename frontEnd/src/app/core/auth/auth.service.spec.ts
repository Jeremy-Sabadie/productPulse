import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthService);
  });

  it('should start without an access token', () => {
    expect(service.getAccessToken()).toBeNull();
  });

  it('should retain an access token in memory', () => {
    service.setAccessToken('test-token');

    expect(service.getAccessToken()).toBe('test-token');
  });

  it('should clear the access token', () => {
    service.setAccessToken('test-token');
    service.clearAccessToken();

    expect(service.getAccessToken()).toBeNull();
  });
});
