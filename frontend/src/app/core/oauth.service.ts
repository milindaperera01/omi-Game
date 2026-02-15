import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class OauthService {
  private readonly verifierKey = 'omi_pkce_verifier';
  private readonly stateKey = 'omi_oauth_state';

  async beginGoogleLogin(): Promise<void> {
    const state = this.randomString(32);
    const verifier = this.randomString(64);
    const challenge = await this.pkceChallenge(verifier);

    sessionStorage.setItem(this.verifierKey, verifier);
    sessionStorage.setItem(this.stateKey, state);

    const params = new URLSearchParams({
      client_id: environment.google.clientId,
      redirect_uri: environment.google.redirectUri,
      response_type: 'code',
      scope: environment.google.scope,
      state,
      code_challenge: challenge,
      code_challenge_method: 'S256',
      access_type: 'offline',
      prompt: 'consent'
    });

    window.location.href = `${environment.google.authEndpoint}?${params.toString()}`;
  }

  consumeVerifier(expectedState: string | null): string {
    const storedState = sessionStorage.getItem(this.stateKey);
    if (!expectedState || !storedState || expectedState !== storedState) {
      throw new Error('OAuth state mismatch');
    }

    const verifier = sessionStorage.getItem(this.verifierKey);
    sessionStorage.removeItem(this.verifierKey);
    sessionStorage.removeItem(this.stateKey);

    if (!verifier) {
      throw new Error('Missing PKCE code verifier');
    }
    return verifier;
  }

  private async pkceChallenge(verifier: string): Promise<string> {
    const data = new TextEncoder().encode(verifier);
    const digest = await crypto.subtle.digest('SHA-256', data);
    return this.base64Url(new Uint8Array(digest));
  }

  private randomString(length: number): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
    const random = crypto.getRandomValues(new Uint8Array(length));
    return Array.from(random)
      .map((x) => chars[x % chars.length])
      .join('');
  }

  private base64Url(bytes: Uint8Array): string {
    const binary = Array.from(bytes)
      .map((b) => String.fromCharCode(b))
      .join('');

    return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }
}
