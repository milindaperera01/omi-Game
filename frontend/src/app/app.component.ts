import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="app-shell">
      <header class="top panel">
        <div>
          <h1>Omi Match Hub</h1>
          <p>Google auth, friends, lobby flow, and game start orchestration</p>
        </div>
        <nav>
          <a routerLink="/dashboard" routerLinkActive="active">Dashboard</a>
          <a routerLink="/login" routerLinkActive="active">Login</a>
        </nav>
      </header>

      <main>
        <router-outlet></router-outlet>
      </main>
    </div>
  `,
  styles: [
    `
      .top {
        padding: 18px 22px;
        margin-bottom: 18px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 14px;
      }

      h1 {
        font-size: clamp(1.6rem, 2.5vw, 2.25rem);
      }

      p {
        margin: 6px 0 0;
        color: var(--ink-soft);
      }

      nav {
        display: flex;
        gap: 8px;
      }

      a {
        text-decoration: none;
        padding: 8px 12px;
        border-radius: 8px;
        font-weight: 700;
      }

      a.active,
      a:hover {
        background: #e7eefb;
      }

      @media (max-width: 800px) {
        .top {
          flex-direction: column;
          align-items: flex-start;
        }
      }
    `
  ]
})
export class AppComponent {}
