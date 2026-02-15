# group-service

Group/lobby service for Omi.

## Features
- Single-player group creation (1 human + 3 bots)
- Multiplayer lobby creation and invite responses
- Auto game creation via game-service once group is ready
- Redis persistence (or in-memory fallback)
- WebSocket lobby updates

## Run

```bash
./mvnw spring-boot:run
```

Default port: `9003`

Run with Redis persistence:

```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.persistence=redis
```

## APIs

Base: `/api/groups`

### Single player

```bash
curl -X POST http://localhost:9003/api/groups/single-player \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p0","displayName":"Milinda"}'
```

### Multiplayer lobby create

```bash
curl -X POST http://localhost:9003/api/groups/multiplayer \
  -H 'Content-Type: application/json' \
  -d '{
    "hostPlayerId":"h1",
    "hostDisplayName":"Host",
    "invitedPlayerIds":["p1","p2","p3"]
  }'
```

### Invite response

```bash
curl -X POST http://localhost:9003/api/groups/{lobbyId}/responses \
  -H 'Content-Type: application/json' \
  -d '{"playerId":"p1","displayName":"P1","accepted":true}'
```

### Get lobby

```bash
curl http://localhost:9003/api/groups/{lobbyId}
```

## WebSocket
- Endpoint: `/ws`
- Topic updates per lobby: `/topic/lobbies/{lobbyId}`
