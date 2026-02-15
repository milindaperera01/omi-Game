# game-service

Spring Boot Omi game service with Redis (or in-memory fallback).

## Prerequisites
- Java 21
- Maven (or use `./mvnw`)
- Redis (only when `app.persistence=redis`)

## Run

From `game-service/`:

```bash
./mvnw spring-boot:run
```

By default it runs with in-memory persistence (`app.persistence=memory`).

## Run with Redis

Start Redis:

```bash
docker run --name omi-redis -p 6379:6379 -d redis:7
```

Run app with Redis:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.persistence=redis
```

## Bot trump choice (optional)

When trump chooser is a BOT, game-service can call logic-service:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.persistence=redis --app.logic-service.enabled=true --app.logic-service.base-url=http://localhost:8081"
```

Expected endpoint in logic-service:
- `POST /api/logic/trump`
- request body: `{ "gameId": "...", "playerId": "...", "cards": [...] }`
- response body: `{ "trumpSuit": "HEARTS" }`

## REST APIs

Base path: `/api/games`

### 1) Create game

```bash
curl -X POST http://localhost:8080/api/games \
  -H 'Content-Type: application/json' \
  -d '{
    "dealerPlayerId":"p2",
    "players":[
      {"playerId":"p0","displayName":"Milinda","playerType":"HUMAN","team":"RED","seatIndex":0},
      {"playerId":"p1","displayName":"Bot 1","playerType":"BOT","team":"BLUE","seatIndex":1},
      {"playerId":"p2","displayName":"Bot 2","playerType":"BOT","team":"RED","seatIndex":2},
      {"playerId":"p3","displayName":"Bot 3","playerType":"BOT","team":"BLUE","seatIndex":3}
    ]
  }'
```

### 2) Choose trump

```bash
curl -X POST http://localhost:8080/api/games/{gameId}/trump \
  -H 'Content-Type: application/json' \
  -d '{"chooserPlayerId":"p3","trumpSuit":"HEARTS"}'
```

### 3) Play card

```bash
curl -X POST http://localhost:8080/api/games/{gameId}/play \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p3","card":{"suit":"HEARTS","rank":"TEN"}}'
```

### 4) Get state (viewer-filtered hand)

```bash
curl "http://localhost:8080/api/games/{gameId}?viewerPlayerId=p0"
```

## WebSocket

- STOMP endpoint: `/ws`
- Topic prefix: `/topic`
- Game updates after each state change:
  - `/topic/games/{gameId}`
- Human player action prompts:
  - `/topic/players/{playerId}/actions`
  - actionType `CHOOSE_TRUMP` with chooser's first 4 cards
