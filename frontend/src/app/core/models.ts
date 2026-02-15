export interface UserView {
  userId: string;
  email: string;
  displayName: string;
  createdAt: string;
  updatedAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  user: UserView;
}

export interface FriendsView {
  userId: string;
  friendEmails: string[];
}

export interface LobbyPlayerView {
  playerId: string;
  displayName: string;
  playerType: 'HUMAN' | 'BOT';
  seatIndex: number;
  team: 'RED' | 'BLUE';
  accepted: boolean;
}

export interface LobbyView {
  lobbyId: string;
  mode: 'SINGLE_PLAYER' | 'MULTIPLAYER';
  status: 'WAITING_FOR_PLAYERS' | 'READY' | 'GAME_CREATED' | 'CANCELLED';
  hostPlayerId: string;
  gameId?: string;
  players: LobbyPlayerView[];
}

export interface SessionState {
  accessToken: string;
  refreshToken: string;
  accessTokenExpiresAt: string;
  refreshTokenExpiresAt: string;
  user: UserView;
}

export type Suit = 'CLUBS' | 'DIAMONDS' | 'HEARTS' | 'SPADES';
export type Rank = 'SEVEN' | 'EIGHT' | 'NINE' | 'TEN' | 'JACK' | 'QUEEN' | 'KING' | 'ACE';

export interface CardView {
  suit: Suit;
  rank: Rank;
}

export interface PlayedCardView {
  playerId: string;
  seatIndex: number;
  card: CardView;
}

export interface GamePlayerView {
  playerId: string;
  displayName: string;
  seatIndex: number;
  playerType: 'HUMAN' | 'BOT';
  team: 'RED' | 'BLUE';
  hand: CardView[];
  handCount: number;
}

export interface GameView {
  gameId: string;
  status: 'WAITING_TRUMP' | 'IN_PROGRESS' | 'HAND_COMPLETE' | 'MATCH_COMPLETE';
  trumpSuit: Suit | null;
  dealerPlayerId: string;
  trumpChooserPlayerId: string;
  currentTurnPlayerId: string | null;
  leaderPlayerId: string | null;
  currentTrickNumber: number;
  currentTrick: PlayedCardView[];
  handTricksRed: number;
  handTricksBlue: number;
  matchHandsRed: number;
  matchHandsBlue: number;
  players: GamePlayerView[];
}
