import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { LobbyView } from '../core/models';

@Component({
  standalone: true,
  template: `
    <section class="panel card">
      @if (!lobby) {
        <h2>Lobby Loading</h2>
        <p>{{ message }}</p>
        @if (error) {
          <p class="error">{{ error }}</p>
        }
      } @else {
        <div class="head">
          <div>
            <h2>Lobby {{ lobby.lobbyId }}</h2>
            <p>Mode: {{ lobby.mode }} | Status: {{ lobby.status }}</p>
          </div>
          @if (lobby.status === 'GAME_CREATED') {
            <span class="badge ok">Game ready: {{ lobby.gameId }}</span>
          }
          @if (lobby.status === 'WAITING_FOR_PLAYERS') {
            <span class="badge warn">Waiting for players</span>
          }
          @if (lobby.status === 'CANCELLED') {
            <span class="badge err">Lobby cancelled</span>
          }
        </div>

        <div class="actions">
          @if (lobby.status === 'GAME_CREATED' && lobby.gameId) {
            <button class="btn btn-primary" (click)="openGame()">Start Game</button>
          }
          <button class="btn btn-secondary" (click)="refreshNow()">Refresh</button>
          <button class="btn btn-secondary" (click)="goDashboard()">Back</button>
        </div>

        <div class="players">
          @for (player of lobby.players; track player.playerId) {
            <article class="player">
              <div>
                <h4>{{ player.displayName }}</h4>
                <p>{{ player.playerId }} | seat {{ player.seatIndex }} | {{ player.team }}</p>
              </div>
              <span class="badge" [class.ok]="player.accepted" [class.warn]="!player.accepted">
                {{ player.accepted ? 'Accepted' : 'Pending' }}
              </span>
              @if (lobby.mode === 'MULTIPLAYER' && !player.accepted && player.playerId !== lobby.hostPlayerId) {
                <div class="simulate">
                  <button class="btn btn-primary" (click)="simulateResponse(player.playerId, true)">Simulate Accept</button>
                  <button class="btn btn-secondary" (click)="simulateResponse(player.playerId, false)">Simulate Decline</button>
                </div>
              }
            </article>
          }
        </div>
      }

      @if (lobby && error) {
        <p class="error">{{ error }}</p>
      }
    </section>
  `,
  styles: [
    `
      .card {
        padding: 20px;
      }

      .head {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
        margin-bottom: 10px;
      }

      .actions {
        display: flex;
        gap: 8px;
        margin-bottom: 14px;
      }

      .players {
        display: grid;
        gap: 10px;
      }

      .player {
        display: grid;
        grid-template-columns: 1fr auto;
        gap: 8px;
        align-items: center;
        border: 1px solid #d6dded;
        border-radius: 12px;
        background: #fff;
        padding: 12px;
      }

      .player h4 {
        margin: 0;
      }

      .player p {
        margin: 4px 0 0;
        font-size: 13px;
        color: var(--ink-soft);
      }

      .simulate {
        grid-column: 1 / -1;
        display: flex;
        gap: 8px;
      }

      .error {
        color: #9f1d1d;
      }

      @media (max-width: 800px) {
        .head {
          flex-direction: column;
          align-items: flex-start;
        }

        .player {
          grid-template-columns: 1fr;
        }
      }
    `
  ]
})
export class LobbyPageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  lobby: LobbyView | null = null;
  message = 'Fetching lobby state...';
  error = '';
  private poller?: Subscription;

  async ngOnInit(): Promise<void> {
    if (!this.auth.session) {
      await this.router.navigate(['/login']);
      return;
    }

    await this.refreshNow();
    this.poller = interval(2500).subscribe(() => {
      this.refreshNow();
    });
  }

  ngOnDestroy(): void {
    this.poller?.unsubscribe();
  }

  async refreshNow(): Promise<void> {
    const lobbyId = this.route.snapshot.paramMap.get('lobbyId');
    if (!lobbyId) {
      this.error = 'Missing lobby ID in route.';
      return;
    }

    try {
      const loaded = await this.api.getLobby(lobbyId);
      this.lobby = {
        ...loaded,
        players: [...loaded.players]
      };
      this.error = '';
      this.message = `Lobby loaded: ${this.lobby.lobbyId}`;
      console.log('Lobby loaded', this.lobby);
      this.cdr.detectChanges();
    } catch (err: unknown) {
      this.error = this.formatError(err, 'Failed to load lobby. Verify token and lobby ID.');
      // Keep this visible for dev diagnostics instead of silently staying in loading state.
      console.error('Lobby refresh failed', err);
      this.cdr.detectChanges();
    }
  }

  async simulateResponse(playerId: string, accepted: boolean): Promise<void> {
    if (!this.lobby) {
      return;
    }

    const displayName = accepted ? this.prettyName(playerId) : 'Declined Player';

    try {
      this.lobby = await this.api.respondInvite(this.lobby.lobbyId, playerId, displayName, accepted);
      this.error = '';
    } catch {
      this.error = 'Failed to submit simulated response.';
    }
  }

  goDashboard(): void {
    this.router.navigate(['/dashboard']);
  }

  openGame(): void {
    if (!this.lobby?.gameId) {
      return;
    }
    this.router.navigate(['/game', this.lobby.gameId], {
      queryParams: {
        lobbyId: this.lobby.lobbyId,
        viewerPlayerId: this.auth.session?.user.userId ?? ''
      }
    });
  }

  private prettyName(playerId: string): string {
    const name = playerId.split('@')[0] ?? playerId;
    return name.charAt(0).toUpperCase() + name.slice(1);
  }

  private formatError(err: unknown, fallback: string): string {
    if (typeof err === 'object' && err !== null && 'status' in err) {
      const e = err as { status?: number; error?: unknown; message?: string };
      let payload = '';
      if (typeof e.error === 'string') {
        payload = e.error;
      } else if (e.error != null) {
        try {
          payload = JSON.stringify(e.error);
        } catch {
          payload = String(e.error);
        }
      }
      return `Failed to load lobby (${e.status ?? 'n/a'}): ${payload || e.message || 'unknown error'}`;
    }
    if (err instanceof Error) {
      return `${fallback} (${err.message})`;
    }
    return fallback;
  }
}
