import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly storageKey = 'productpulse.accessToken';

  getAccessToken(): string | null {
    return sessionStorage.getItem(this.storageKey);
  }

  setAccessToken(token: string): void {
    sessionStorage.setItem(this.storageKey, token);
  }

  clearAccessToken(): void {
    sessionStorage.removeItem(this.storageKey);
  }
}
