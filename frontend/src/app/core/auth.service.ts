import { inject, Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { ApiService } from './api.service';
import { AuthResponse, SessionState } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly storageKey = 'omi_session';
  private readonly state$ = new BehaviorSubject<SessionState | null>(this.loadSession());

  sessionChanges = this.state$.asObservable();

  get session(): SessionState | null {
    return this.state$.value;
  }

  get accessToken(): string | null {
    return this.state$.value?.accessToken ?? null;
  }

  async completeGoogleExchange(code: string, codeVerifier: string): Promise<void> {
    const auth = await this.api.exchangeGoogleCode(code, codeVerifier);
    this.setSession(auth);
  }

  async refreshTokens(): Promise<boolean> {
    const current = this.state$.value;
    if (!current?.refreshToken) {
      return false;
    }

    try {
      const auth = await this.api.refresh(current.refreshToken);
      this.setSession(auth);
      return true;
    } catch {
      this.logout();
      return false;
    }
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
    this.state$.next(null);
  }

  private setSession(auth: AuthResponse): void {
    const session: SessionState = {
      accessToken: auth.accessToken,
      refreshToken: auth.refreshToken,
      accessTokenExpiresAt: auth.accessTokenExpiresAt,
      refreshTokenExpiresAt: auth.refreshTokenExpiresAt,
      user: auth.user
    };

    localStorage.setItem(this.storageKey, JSON.stringify(session));
    this.state$.next(session);
  }

  private loadSession(): SessionState | null {
    const raw = localStorage.getItem(this.storageKey);
    if (!raw) {
      return null;
    }
    try {
      const parsed = JSON.parse(raw) as unknown;
      const obj = typeof parsed === 'string' ? JSON.parse(parsed) as unknown : parsed;
      if (!obj || typeof obj !== 'object') {
        throw new Error('Invalid session payload');
      }
      return obj as SessionState;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
