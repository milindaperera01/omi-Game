import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

@Component({
  standalone: true,
  imports: [FormsModule],
  template: `
    @if (!auth.session) {
      <section class="panel card">
        <h2>Authentication Required</h2>
        <p>Please sign in with Google first.</p>
        <button class="btn btn-primary" (click)="goLogin()">Go to Login</button>
      </section>
    } @else {
      <section class="grid">
        <article class="panel card profile">
          <div>
            <h2>{{ auth.session.user.displayName }}</h2>
            <p>{{ auth.session.user.email }}</p>
          </div>
          <span class="badge ok">Logged in</span>
          <button class="btn btn-secondary" (click)="logout()">Logout</button>
        </article>

        <article class="panel card">
          <h3>Game Mode</h3>
          <p>Single-player starts immediately with 3 bots.</p>
          <button class="btn btn-primary" (click)="startSinglePlayer()" [disabled]="loadingSingle">
            {{ loadingSingle ? 'Starting...' : 'Start Single Player' }}
          </button>
          @if (singleError) {
            <p class="error">{{ singleError }}</p>
          }
        </article>

        <article class="panel card">
          <h3>Friends</h3>
          <p>Add friends by email and choose exactly 3 for multiplayer invites.</p>
          <div class="friend-form">
            <input class="input" [(ngModel)]="friendEmail" type="email" placeholder="friend@email.com" />
            <button class="btn btn-secondary" (click)="addFriend()" [disabled]="addingFriend">Add</button>
          </div>

          @if (friendError) {
            <p class="error">{{ friendError }}</p>
          }

          <div class="friend-list">
            @for (friend of friends; track friend) {
              <label>
                <input
                  type="checkbox"
                  [checked]="selected.has(friend)"
                  (change)="toggleFriend(friend, $event)"
                />
                <span>{{ friend }}</span>
              </label>
            }
          </div>

          <p class="count">Selected: {{ selected.size }}/3</p>

          <button class="btn btn-primary" (click)="startMultiplayer()" [disabled]="selected.size !== 3 || loadingMulti">
            {{ loadingMulti ? 'Creating Lobby...' : 'Start Multiplayer Lobby' }}
          </button>

          @if (multiError) {
            <p class="error">{{ multiError }}</p>
          }
        </article>
      </section>
    }
  `,
  styles: [
    `
      .grid {
        display: grid;
        gap: 14px;
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }

      .card {
        padding: 20px;
      }

      .profile {
        grid-column: 1 / -1;
        display: flex;
        align-items: center;
        gap: 10px;
      }

      .profile button {
        margin-left: auto;
      }

      h3 {
        margin-bottom: 8px;
      }

      p {
        margin-top: 0;
        color: var(--ink-soft);
      }

      .friend-form {
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 8px;
      }

      .friend-list {
        margin: 12px 0;
        display: grid;
        gap: 6px;
        max-height: 220px;
        overflow: auto;
      }

      .friend-list label {
        display: flex;
        gap: 8px;
        align-items: center;
        padding: 8px 10px;
        border-radius: 8px;
        background: #f6f8ff;
      }

      .count {
        font-size: 13px;
      }

      .error {
        color: #9f1d1d;
      }

      @media (max-width: 840px) {
        .grid {
          grid-template-columns: 1fr;
        }

        .profile {
          flex-wrap: wrap;
        }

        .profile button {
          margin-left: 0;
        }
      }
    `
  ]
})
export class DashboardComponent implements OnInit {
  readonly auth = inject(AuthService);

  private readonly api = inject(ApiService);
  private readonly router = inject(Router);

  friends: string[] = [];
  selected = new Set<string>();
  friendEmail = '';

  loadingSingle = false;
  loadingMulti = false;
  addingFriend = false;

  friendError = '';
  singleError = '';
  multiError = '';

  async ngOnInit(): Promise<void> {
    if (!this.auth.session) {
      return;
    }
    await this.loadFriends();
  }

  goLogin(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }

  async loadFriends(): Promise<void> {
    if (!this.auth.session) {
      return;
    }
    this.friendError = '';
    try {
      const view = await this.api.getFriends(this.auth.session.user.userId);
      this.friends = view.friendEmails;
    } catch {
      this.friendError = 'Failed to load friends list.';
    }
  }

  async addFriend(): Promise<void> {
    if (!this.auth.session || !this.friendEmail.trim()) {
      return;
    }

    this.addingFriend = true;
    this.friendError = '';

    try {
      const view = await this.api.addFriend(this.auth.session.user.userId, this.friendEmail.trim());
      this.friends = view.friendEmails;
      this.friendEmail = '';
    } catch {
      this.friendError = 'Failed to add friend. Check email format and token.';
    } finally {
      this.addingFriend = false;
    }
  }

  toggleFriend(friend: string, event: Event): void {
    const checked = (event.target as HTMLInputElement).checked;

    if (checked && this.selected.size >= 3 && !this.selected.has(friend)) {
      (event.target as HTMLInputElement).checked = false;
      return;
    }

    if (checked) {
      this.selected.add(friend);
    } else {
      this.selected.delete(friend);
    }
  }

  async startSinglePlayer(): Promise<void> {
    if (!this.auth.session) {
      return;
    }

    this.loadingSingle = true;
    this.singleError = '';
    try {
      const lobby = await this.api.createSinglePlayer(this.auth.session.user.userId, this.auth.session.user.displayName);
      await this.router.navigate(['/lobby', lobby.lobbyId]);
    } catch {
      this.singleError = 'Failed to start single-player lobby.';
    } finally {
      this.loadingSingle = false;
    }
  }

  async startMultiplayer(): Promise<void> {
    if (!this.auth.session || this.selected.size !== 3) {
      return;
    }

    this.loadingMulti = true;
    this.multiError = '';

    try {
      const invitedPlayerIds = Array.from(this.selected);
      const lobby = await this.api.createMultiplayer(
        this.auth.session.user.userId,
        this.auth.session.user.displayName,
        invitedPlayerIds
      );
      await this.router.navigate(['/lobby', lobby.lobbyId]);
    } catch {
      this.multiError = 'Failed to create multiplayer lobby.';
    } finally {
      this.loadingMulti = false;
    }
  }
}
