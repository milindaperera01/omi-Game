import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { interval, Subscription } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { CardView, GamePlayerView, GameView, PlayedCardView, Suit } from '../core/models';

@Component({
  standalone: true,
  template: `
    <section class="panel card">
      <div class="head">
        <div class="meta">
          <span class="badge">Trump: {{ game?.trumpSuit ? suitSymbol(game!.trumpSuit!) : '-' }}</span>
          <span class="badge">Turn: {{ currentTurnName }}</span>
          <span class="badge">RED {{ game?.handTricksRed ?? 0 }} : {{ game?.handTricksBlue ?? 0 }} BLUE</span>
        </div>
        <button class="btn btn-secondary" (click)="goBack()">Back</button>
      </div>

      @if (hasPendingReveals) {
        <div class="reveal-row">
          <button class="btn btn-primary" (click)="revealNextCard()">Next</button>
          <span class="hint">Reveal next played card</span>
        </div>
      }

      @if (game) {
        <div class="table-wrap">
          <div class="table-box">
            <div class="seat top">
              <div class="name">{{ topPlayer?.displayName ?? '-' }}</div>
              <div class="hand-back">
                @for (_ of cardBacks(topPlayer?.handCount ?? 0); track $index) {
                  <span class="mini-back"></span>
                }
              </div>
            </div>

            <div class="seat left">
              <div class="name">{{ leftPlayer?.displayName ?? '-' }}</div>
              <div class="hand-back vertical">
                @for (_ of cardBacks(leftPlayer?.handCount ?? 0); track $index) {
                  <span class="mini-back"></span>
                }
              </div>
            </div>

            <div class="seat right">
              <div class="name">{{ rightPlayer?.displayName ?? '-' }}</div>
              <div class="hand-back vertical">
                @for (_ of cardBacks(rightPlayer?.handCount ?? 0); track $index) {
                  <span class="mini-back"></span>
                }
              </div>
            </div>

            <div class="seat bottom">
              <div class="name">{{ bottomPlayer?.displayName ?? '-' }}</div>
            </div>

            @for (played of displayedTrick; track played.playerId + played.card.suit + played.card.rank) {
              <div
                class="table-card"
                [class.top]="positionClass(played.playerId) === 'top'"
                [class.bottom]="positionClass(played.playerId) === 'bottom'"
                [class.left]="positionClass(played.playerId) === 'left'"
                [class.right]="positionClass(played.playerId) === 'right'"
              >
                <div class="playing-card small" [class.red-suit]="isRedSuit(played.card.suit)">
                  <div class="rank">{{ rankShort(played.card.rank) }}</div>
                  <div class="suit">{{ suitSymbol(played.card.suit) }}</div>
                </div>
              </div>
            }
          </div>
        </div>
      } @else {
        <div class="loading-box">
          <p>Loading game state...</p>
          <button class="btn btn-secondary" (click)="refreshNow()">Retry</button>
        </div>
      }

      @if (error) {
        <p class="error">{{ error }}</p>
      }

      @if (game) {
        @if (isMyTrumpChoice) {
          <h3>Choose Trump</h3>
          <div class="trump-actions">
            @for (suit of suits; track suit) {
              <button class="btn btn-primary" (click)="chooseTrump(suit)" [disabled]="loadingAction">
                {{ suitSymbol(suit) }} {{ suit }}
              </button>
            }
          </div>
        }

        <h3>My Hand</h3>
        <div class="hand">
          @for (card of myHand; track card.suit + '-' + card.rank) {
            <button
              class="card-btn"
              [class.red-suit]="isRedSuit(card.suit)"
              [class.selected]="isSelected(card)"
              (click)="selectCard(card)"
              [disabled]="!isMyTurn || loadingAction"
            >
              <span class="rank">{{ rankShort(card.rank) }}</span>
              <span class="suit">{{ suitSymbol(card.suit) }}</span>
            </button>
          }
        </div>

        <div class="play-actions">
          <button class="btn btn-primary" (click)="playSelectedCard()" [disabled]="!selectedCard || !isMyTurn || loadingAction">
            {{ loadingAction ? 'Submitting...' : 'Play Selected Card' }}
          </button>
        </div>
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
        gap: 12px;
      }

      .meta {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
      }

      .reveal-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin: 8px 0 12px;
      }

      .hint {
        font-size: 13px;
        color: var(--ink-soft);
      }

      .table-wrap {
        display: flex;
        justify-content: center;
        margin: 10px 0 16px;
      }

      .table-box {
        position: relative;
        width: min(640px, 100%);
        aspect-ratio: 16 / 9;
        border-radius: 16px;
        border: 2px solid #1f5f2b;
        background: radial-gradient(circle at 30% 20%, #3e9960, #2e7d47 60%, #25653a);
        box-shadow: inset 0 0 0 3px rgba(255, 255, 255, 0.08);
      }

      .loading-box {
        border: 1px solid #d6dded;
        border-radius: 12px;
        padding: 12px;
        margin: 8px 0 14px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 8px;
      }

      .seat {
        position: absolute;
        color: #fff;
        text-shadow: 0 1px 1px rgba(0, 0, 0, 0.45);
      }

      .seat .name {
        font-weight: 700;
        font-size: 14px;
      }

      .top {
        top: 12px;
        left: 50%;
        transform: translateX(-50%);
        text-align: center;
      }

      .bottom {
        bottom: 12px;
        left: 50%;
        transform: translateX(-50%);
        text-align: center;
      }

      .left {
        left: 12px;
        top: 50%;
        transform: translateY(-50%);
      }

      .right {
        right: 12px;
        top: 50%;
        transform: translateY(-50%);
        text-align: right;
      }

      .hand-back {
        display: flex;
        justify-content: center;
        gap: 2px;
        margin-top: 4px;
      }

      .hand-back.vertical {
        flex-direction: column;
        align-items: flex-start;
      }

      .mini-back {
        width: 18px;
        height: 24px;
        border-radius: 4px;
        border: 1px solid rgba(255, 255, 255, 0.55);
        background: repeating-linear-gradient(45deg, #1e3a8a, #1e3a8a 3px, #2563eb 3px, #2563eb 6px);
      }

      .table-card {
        position: absolute;
      }

      .table-card.top {
        top: 34%;
        left: 50%;
        transform: translate(-50%, -50%);
      }

      .table-card.bottom {
        bottom: 30%;
        left: 50%;
        transform: translate(-50%, 50%);
      }

      .table-card.left {
        left: 34%;
        top: 50%;
        transform: translate(-50%, -50%);
      }

      .table-card.right {
        right: 34%;
        top: 50%;
        transform: translate(50%, -50%);
      }

      .trump-actions {
        display: flex;
        gap: 8px;
        margin-bottom: 12px;
      }

      .hand {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: 10px;
      }

      .card-btn {
        border: 1px solid #ccd5ea;
        border-radius: 10px;
        background: #fff;
        width: 74px;
        height: 102px;
        padding: 8px;
        cursor: pointer;
        display: grid;
        align-content: space-between;
        text-align: left;
        font-weight: 700;
      }

      .card-btn.selected {
        border-color: #2667ff;
        box-shadow: 0 0 0 2px rgba(38, 103, 255, 0.15);
      }

      .card-btn .rank {
        font-size: 18px;
        line-height: 1;
      }

      .card-btn .suit {
        font-size: 24px;
        line-height: 1;
        justify-self: end;
      }

      .play-actions {
        margin-bottom: 14px;
      }

      .error {
        color: #9f1d1d;
      }

      .red-suit {
        color: #c62828;
      }

      .playing-card {
        width: 56px;
        height: 76px;
        border: 1px solid #cdd6eb;
        border-radius: 8px;
        background: #fff;
        box-shadow: 0 1px 2px rgba(16, 24, 40, 0.08);
        display: grid;
        align-content: space-between;
        padding: 6px;
      }

      .playing-card.small .rank {
        font-size: 14px;
        line-height: 1;
      }

      .playing-card.small .suit {
        font-size: 18px;
        line-height: 1;
        justify-self: end;
      }

      @media (max-width: 900px) {
        .head {
          flex-direction: column;
          align-items: flex-start;
        }

        .table-box {
          aspect-ratio: 9 / 10;
        }
      }
    `
  ]
})
export class GamePageComponent implements OnInit, OnDestroy {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(ApiService);
  private readonly auth = inject(AuthService);
  private readonly cdr = inject(ChangeDetectorRef);

  game: GameView | null = null;
  error = '';
  loadingAction = false;
  selectedCard: CardView | null = null;
  suits: Suit[] = ['CLUBS', 'DIAMONDS', 'HEARTS', 'SPADES'];
  displayedTrick: PlayedCardView[] = [];
  private lastTrickFingerprint = '';
  private retryPoller?: Subscription;

  get viewerPlayerId(): string {
    const fromSession = this.auth.session?.user.userId;
    const fromRoute = this.route.snapshot.queryParamMap.get('viewerPlayerId');
    return fromSession || fromRoute || '';
  }

  get isMyTurn(): boolean {
    return this.game?.currentTurnPlayerId === this.viewerPlayerId;
  }

  get isMyTrumpChoice(): boolean {
    return this.game?.status === 'WAITING_TRUMP' && this.game?.trumpChooserPlayerId === this.viewerPlayerId;
  }

  get myHand(): CardView[] {
    const me = this.game?.players.find((p) => p.playerId === this.viewerPlayerId);
    return me?.hand ?? [];
  }

  get currentTurnName(): string {
    if (!this.game?.currentTurnPlayerId) {
      return '-';
    }
    return this.playerName(this.game.currentTurnPlayerId);
  }

  get hasPendingReveals(): boolean {
    return !!this.game && this.displayedTrick.length < this.game.currentTrick.length;
  }

  get topPlayer(): GamePlayerView | undefined {
    return this.playerBySeat(2);
  }

  get bottomPlayer(): GamePlayerView | undefined {
    return this.playerBySeat(0);
  }

  get leftPlayer(): GamePlayerView | undefined {
    return this.playerBySeat(1);
  }

  get rightPlayer(): GamePlayerView | undefined {
    return this.playerBySeat(3);
  }

  async ngOnInit(): Promise<void> {
    if (!this.auth.session) {
      await this.router.navigate(['/login']);
      return;
    }
    await this.refreshNow();
    this.retryPoller = interval(1000).subscribe(() => {
      if (!this.game) {
        void this.refreshNow();
      }
    });
  }

  ngOnDestroy(): void {
    this.retryPoller?.unsubscribe();
  }

  async refreshNow(): Promise<void> {
    const gameId = this.route.snapshot.paramMap.get('gameId');
    if (!gameId || !this.viewerPlayerId) {
      this.error = 'Missing game ID or user session.';
      return;
    }

    try {
      const loaded = await this.api.getGame(gameId, this.viewerPlayerId);
      this.game = loaded;
      this.error = '';
      this.syncDisplayedTrick();
      if (this.selectedCard && !this.myHand.some((c) => c.suit === this.selectedCard?.suit && c.rank === this.selectedCard?.rank)) {
        this.selectedCard = null;
      }
    } catch (err: unknown) {
      this.error = err instanceof Error ? err.message : 'Failed to load game.';
    }
  }

  selectCard(card: CardView): void {
    this.selectedCard = card;
  }

  isSelected(card: CardView): boolean {
    return this.selectedCard?.suit === card.suit && this.selectedCard?.rank === card.rank;
  }

  async playSelectedCard(): Promise<void> {
    if (!this.game || !this.selectedCard || !this.isMyTurn) {
      return;
    }
    this.loadingAction = true;
    this.error = '';
    try {
      this.game = await this.api.playCard(this.game.gameId, this.viewerPlayerId, this.selectedCard, this.viewerPlayerId);
      this.selectedCard = null;
      this.loadingAction = false;
      this.syncDisplayedTrick();
      this.cdr.detectChanges();
      void this.refreshNow();
    } catch (err: unknown) {
      this.error = this.formatActionError(err, 'Failed to play card.');
      this.loadingAction = false;
      this.cdr.detectChanges();
    }
  }

  async chooseTrump(suit: Suit): Promise<void> {
    if (!this.game || !this.isMyTrumpChoice) {
      return;
    }
    this.loadingAction = true;
    this.error = '';
    try {
      this.game = await this.api.chooseTrump(this.game.gameId, this.viewerPlayerId, suit, this.viewerPlayerId);
      this.loadingAction = false;
      this.syncDisplayedTrick();
      this.cdr.detectChanges();
      void this.refreshNow();
    } catch (err: unknown) {
      this.error = this.formatActionError(err, 'Failed to choose trump.');
      this.loadingAction = false;
      this.cdr.detectChanges();
    }
  }

  goBack(): void {
    const lobbyId = this.route.snapshot.queryParamMap.get('lobbyId');
    if (lobbyId) {
      this.router.navigate(['/lobby', lobbyId]);
      return;
    }
    this.router.navigate(['/dashboard']);
  }

  revealNextCard(): void {
    if (!this.game || this.displayedTrick.length >= this.game.currentTrick.length) {
      return;
    }
    this.displayedTrick = this.game.currentTrick.slice(0, this.displayedTrick.length + 1);
  }

  cardBacks(count: number): number[] {
    return Array.from({ length: Math.min(count, 8) }, (_, i) => i);
  }

  playerName(playerId: string): string {
    return this.game?.players.find((p) => p.playerId === playerId)?.displayName ?? playerId;
  }

  positionClass(playerId: string): 'top' | 'bottom' | 'left' | 'right' {
    const seat = this.game?.players.find((p) => p.playerId === playerId)?.seatIndex;
    if (seat === 0) {
      return 'bottom';
    }
    if (seat === 1) {
      return 'left';
    }
    if (seat === 2) {
      return 'top';
    }
    return 'right';
  }

  suitSymbol(suit: Suit): string {
    switch (suit) {
      case 'CLUBS':
        return '♣';
      case 'DIAMONDS':
        return '♦';
      case 'HEARTS':
        return '♥';
      case 'SPADES':
        return '♠';
    }
  }

  rankShort(rank: CardView['rank']): string {
    switch (rank) {
      case 'SEVEN':
        return '7';
      case 'EIGHT':
        return '8';
      case 'NINE':
        return '9';
      case 'TEN':
        return '10';
      case 'JACK':
        return 'J';
      case 'QUEEN':
        return 'Q';
      case 'KING':
        return 'K';
      case 'ACE':
        return 'A';
    }
  }

  isRedSuit(suit: Suit): boolean {
    return suit === 'HEARTS' || suit === 'DIAMONDS';
  }

  private playerBySeat(seat: number): GamePlayerView | undefined {
    return this.game?.players.find((p) => p.seatIndex === seat);
  }

  private formatActionError(err: unknown, fallback: string): string {
    if (typeof err === 'object' && err !== null && 'status' in err) {
      const e = err as { status?: number; error?: unknown; message?: string };
      if (typeof e.error === 'object' && e.error !== null && 'message' in e.error) {
        const msg = (e.error as { message?: string }).message;
        return `${fallback} (${e.status ?? 'n/a'}): ${msg ?? 'unknown error'}`;
      }
      return `${fallback} (${e.status ?? 'n/a'})`;
    }
    return err instanceof Error ? `${fallback} ${err.message}` : fallback;
  }

  private syncDisplayedTrick(): void {
    if (!this.game) {
      this.displayedTrick = [];
      this.lastTrickFingerprint = '';
      return;
    }

    const fingerprint = [
      this.game.gameId,
      this.game.currentTrickNumber,
      this.game.currentTrick.map((c) => `${c.playerId}:${c.card.suit}-${c.card.rank}`).join('|')
    ].join('::');

    if (this.lastTrickFingerprint === '') {
      this.displayedTrick = [...this.game.currentTrick];
      this.lastTrickFingerprint = fingerprint;
      return;
    }

    if (fingerprint !== this.lastTrickFingerprint) {
      const visibleCount = this.displayedTrick.length;
      this.displayedTrick = this.game.currentTrick.slice(0, Math.min(visibleCount, this.game.currentTrick.length));
      this.lastTrickFingerprint = fingerprint;
    }
  }
}
