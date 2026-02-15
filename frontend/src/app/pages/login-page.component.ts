import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { OauthService } from '../core/oauth.service';
import { AuthService } from '../core/auth.service';

@Component({
  standalone: true,
  template: `
    <section class="panel wrap">
      <h2>Sign in</h2>
      <p>Use Google OAuth to create or reuse a user and get app JWT tokens.</p>

      <button class="btn btn-primary" (click)="loginWithGoogle()">Continue with Google</button>

      @if (error) {
        <p class="error">{{ error }}</p>
      }

      @if (auth.session; as session) {
        <div class="result">
          <p><strong>Signed in as:</strong> {{ session.user.displayName }} ({{ session.user.email }})</p>
          <button class="btn btn-secondary" (click)="goDashboard()">Go to Dashboard</button>
        </div>
      }
    </section>
  `,
  styles: [
    `
      .wrap {
        padding: 24px;
        max-width: 640px;
      }

      p {
        color: var(--ink-soft);
      }

      .error {
        color: #9f1d1d;
      }

      .result {
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid #d4dbea;
      }
    `
  ]
})
export class LoginPageComponent {
  private readonly oauth = inject(OauthService);
  private readonly router = inject(Router);
  readonly auth = inject(AuthService);

  error = '';

  async loginWithGoogle(): Promise<void> {
    this.error = '';
    try {
      await this.oauth.beginGoogleLogin();
    } catch (error) {
      this.error = error instanceof Error ? error.message : 'Failed to start Google login';
    }
  }

  goDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}
