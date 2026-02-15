import { Routes } from '@angular/router';
import { LoginPageComponent } from './pages/login-page.component';
import { AuthCallbackComponent } from './pages/auth-callback.component';
import { DashboardComponent } from './pages/dashboard.component';
import { LobbyPageComponent } from './pages/lobby-page.component';
import { GamePageComponent } from './pages/game-page.component';

export const appRoutes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  { path: 'login', component: LoginPageComponent },
  { path: 'auth/google/callback', component: AuthCallbackComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'lobby/:lobbyId', component: LobbyPageComponent },
  { path: 'game/:gameId', component: GamePageComponent },
  { path: '**', redirectTo: 'dashboard' }
];
