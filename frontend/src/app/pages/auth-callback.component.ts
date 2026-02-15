import { Component, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { OauthService } from '../core/oauth.service';
import { AuthService } from '../core/auth.service';

@Component({
  standalone: true,
  template: `
    <section class="panel wrap">
      <h2>Completing Sign-In</h2>
      <p>{{ message }}</p>
      @if (error) {
        <p class="error">{{ error }}</p>
        <button class="btn btn-secondary" (click)="goLogin()">Back to Login</button>
      }
    </section>
  `,
  styles: [
    `
      .wrap {
        padding: 24px;
        max-width: 640px;
      }

      .error {
        color: #9f1d1d;
      }
    `
  ]
})
export class AuthCallbackComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly oauth = inject(OauthService);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  message = 'Validating authorization response...';
  error = '';

  async ngOnInit(): Promise<void> {
    const code = this.route.snapshot.queryParamMap.get('code');
    const state = this.route.snapshot.queryParamMap.get('state');
    const errorParam = this.route.snapshot.queryParamMap.get('error');

    if (errorParam) {
      this.error = `Google OAuth returned error: ${errorParam}`;
      this.message = 'Authentication failed.';
      return;
    }

    if (!code) {
      this.error = 'Missing authorization code in callback URL.';
      this.message = 'Authentication failed.';
      return;
    }

    try {
      const verifier = this.oauth.consumeVerifier(state);
      await this.auth.completeGoogleExchange(code, verifier);
      this.message = 'Login successful. Redirecting...';
      await this.router.navigate(['/dashboard']);
    } catch (error) {
      if (typeof error === 'object' && error !== null && 'status' in error) {
        const e = error as { status?: number; error?: unknown; message?: string };
        const payload = typeof e.error === 'string' ? e.error : JSON.stringify(e.error ?? {});
        this.error = `Exchange failed (${e.status ?? 'n/a'}): ${payload || e.message || 'unknown error'}`;
      } else {
        this.error = error instanceof Error ? error.message : 'Failed to complete login';
      }
      this.message = 'Authentication failed.';
    }
  }

  goLogin(): void {
    this.router.navigate(['/login']);
  }
}
