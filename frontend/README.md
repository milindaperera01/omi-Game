# Omi Angular Frontend

Angular frontend for your Omi microservices flow:
- Google login (OAuth code + PKCE)
- User session with access/refresh tokens
- Friend list and friend selection
- Single-player and multiplayer lobby creation
- Lobby polling and simulated invite responses

## Prerequisites

- Node.js 20+
- npm 10+
- Running backend services:
  - `api-gateway` on `9000`
  - `user-service` on `9004`
  - `group-service` on `9003`
  - `game-service` on `9001`
  - `logic-service` on `9002`

## Run

```bash
cd frontend
npm install
npm start
```

App runs on `http://localhost:5173`.

## Important Auth Notes

- `user-service` must have Google OAuth configured.
- In Google Cloud Console, Authorized redirect URI must include exactly:
  - `http://localhost:5173/auth/google/callback`
- Frontend Google client id is set in:
  - `src/environments/environment.ts`

## Main Routes

- `/login` -> start Google sign-in
- `/auth/google/callback` -> completes token exchange
- `/dashboard` -> friends + lobby creation
- `/lobby/:lobbyId` -> lobby status

## Current API Integration

Gateway base URL is configured as:
- `http://localhost:9000`

Change `src/environments/environment.ts` if needed.
