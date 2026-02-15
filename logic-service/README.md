# logic-service

Stateless decision engine for Omi BOT players.

## Run

From `logic-service/`:

```bash
./mvnw spring-boot:run
```

Service runs on `http://localhost:8080` by default.

## API

Base path: `/api/logic`

### 1) Choose trump

`POST /api/logic/trump`

Example:

```bash
curl -X POST http://localhost:8080/api/logic/trump \
  -H 'Content-Type: application/json' \
  -d '{
    "botPlayerId":"bot-1",
    "seatIndex":1,
    "firstFourCards":[
      {"suit":"HEARTS","rank":"ACE"},
      {"suit":"HEARTS","rank":"KING"},
      {"suit":"SPADES","rank":"SEVEN"},
      {"suit":"CLUBS","rank":"SEVEN"}
    ]
  }'
```

Example response:

```json
{
  "botPlayerId": "bot-1",
  "chosenSuit": "HEARTS",
  "reason": "Selected by suit score heuristic"
}
```

### 2) Choose move

`POST /api/logic/move`

Example:

```bash
curl -X POST http://localhost:8080/api/logic/move \
  -H 'Content-Type: application/json' \
  -d '{
    "gameId":"f19d93db-7792-43ba-9722-f68d3968f2d9",
    "botPlayerId":"bot-1",
    "botSeatIndex":1,
    "trumpSuit":"SPADES",
    "hand":[
      {"suit":"HEARTS","rank":"KING"},
      {"suit":"CLUBS","rank":"ACE"},
      {"suit":"DIAMONDS","rank":"SEVEN"}
    ],
    "currentTrick":[
      {"seatIndex":0,"playerId":"p0","card":{"suit":"HEARTS","rank":"QUEEN"}}
    ]
  }'
```

Example response:

```json
{
  "gameId": "f19d93db-7792-43ba-9722-f68d3968f2d9",
  "botPlayerId": "bot-1",
  "chosenCard": {"suit":"HEARTS","rank":"KING"},
  "reason": "Bot followed suit"
}
```

## Error format

```json
{
  "error": "BAD_REQUEST",
  "message": "...",
  "timestamp": "2026-02-12T00:00:00Z"
}
```
