import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom, timeout } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse, FriendsView, GameView, LobbyView, Rank, Suit } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly api = environment.apiBaseUrl;
  private readonly requestTimeoutMs = 15000;

  exchangeGoogleCode(code: string, codeVerifier: string): Promise<AuthResponse> {
    return firstValueFrom(
      this.http
        .post<AuthResponse>(`${this.api}/api/auth/google/exchange`, { code, codeVerifier })
        .pipe(timeout(20000))
    );
  }

  refresh(refreshToken: string): Promise<AuthResponse> {
    return firstValueFrom(
      this.http
        .post<AuthResponse>(`${this.api}/api/auth/refresh`, { refreshToken })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  getFriends(userId: string): Promise<FriendsView> {
    return firstValueFrom(
      this.http
        .get<FriendsView>(`${this.api}/api/users/${userId}/friends`)
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  addFriend(userId: string, friendEmail: string): Promise<FriendsView> {
    return firstValueFrom(
      this.http
        .post<FriendsView>(`${this.api}/api/users/${userId}/friends`, { friendEmail })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  createSinglePlayer(playerId: string, displayName: string): Promise<LobbyView> {
    return firstValueFrom(
      this.http
        .post<LobbyView>(`${this.api}/api/groups/single-player`, { playerId, displayName })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  createMultiplayer(hostPlayerId: string, hostDisplayName: string, invitedPlayerIds: string[]): Promise<LobbyView> {
    return firstValueFrom(
      this.http
        .post<LobbyView>(`${this.api}/api/groups/multiplayer`, { hostPlayerId, hostDisplayName, invitedPlayerIds })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  getLobby(lobbyId: string): Promise<LobbyView> {
    return firstValueFrom(
      this.http
        .get<LobbyView>(`${this.api}/api/groups/${lobbyId}`)
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  respondInvite(lobbyId: string, playerId: string, displayName: string, accepted: boolean): Promise<LobbyView> {
    return firstValueFrom(
      this.http
        .post<LobbyView>(`${this.api}/api/groups/${lobbyId}/responses`, { playerId, displayName, accepted })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  getGame(gameId: string, viewerPlayerId: string): Promise<GameView> {
    return firstValueFrom(
      this.http
        .get<GameView>(`${this.api}/api/games/${gameId}`, { params: { viewerPlayerId } })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  playCard(gameId: string, playerId: string, card: { suit: Suit; rank: Rank }, viewerPlayerId: string): Promise<GameView> {
    return firstValueFrom(
      this.http
        .post<GameView>(`${this.api}/api/games/${gameId}/play`, { playerId, card }, { params: { viewerPlayerId } })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }

  chooseTrump(gameId: string, chooserPlayerId: string, trumpSuit: Suit, viewerPlayerId: string): Promise<GameView> {
    return firstValueFrom(
      this.http
        .post<GameView>(`${this.api}/api/games/${gameId}/trump`, { chooserPlayerId, trumpSuit }, { params: { viewerPlayerId } })
        .pipe(timeout(this.requestTimeoutMs))
    );
  }
}
