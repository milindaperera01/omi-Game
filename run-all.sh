#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOGIC_DIR="$ROOT_DIR/logic-service"
GAME_DIR="$ROOT_DIR/game-service"
GROUP_DIR="$ROOT_DIR/group-service"
USER_DIR="$ROOT_DIR/user-service"
NOTIFICATION_DIR="$ROOT_DIR/notification-service"
GATEWAY_DIR="$ROOT_DIR/api-gateway"
REDIS_CONTAINER="omi-redis"

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

require_cmd docker
require_cmd bash
require_cmd curl

build_service() {
  local name="$1"
  local dir="$2"
  echo "[info] Pre-building $name"
  (
    cd "$dir"
    ./mvnw -q -DskipTests clean compile
  )
}

wait_for_http() {
  local name="$1"
  local url="$2"
  local attempts="${3:-60}"
  local i
  for ((i = 1; i <= attempts; i++)); do
    if curl -sS -o /dev/null "$url"; then
      echo "[info] $name is reachable at $url"
      return 0
    fi
    sleep 1
  done
  echo "[error] Timed out waiting for $name at $url" >&2
  exit 1
}

start_redis() {
  echo "[info] Ensuring Redis image is available (redis:7)"
  docker pull redis:7 >/dev/null

  if docker ps --format '{{.Names}}' | grep -qx "$REDIS_CONTAINER"; then
    echo "[info] Redis container '$REDIS_CONTAINER' already running"
    return
  fi

  if docker ps -a --format '{{.Names}}' | grep -qx "$REDIS_CONTAINER"; then
    echo "[info] Starting existing Redis container '$REDIS_CONTAINER'"
    docker start "$REDIS_CONTAINER" >/dev/null
  else
    echo "[info] Creating and starting Redis container '$REDIS_CONTAINER'"
    docker run --name "$REDIS_CONTAINER" -p 6379:6379 -d redis:7 >/dev/null
  fi
}

shutdown() {
  echo
  echo "[info] Stopping services..."
  if [[ -n "${GATEWAY_PID:-}" ]] && kill -0 "$GATEWAY_PID" 2>/dev/null; then
    kill "$GATEWAY_PID" 2>/dev/null || true
    wait "$GATEWAY_PID" 2>/dev/null || true
  fi
  if [[ -n "${NOTIFICATION_PID:-}" ]] && kill -0 "$NOTIFICATION_PID" 2>/dev/null; then
    kill "$NOTIFICATION_PID" 2>/dev/null || true
    wait "$NOTIFICATION_PID" 2>/dev/null || true
  fi
  if [[ -n "${USER_PID:-}" ]] && kill -0 "$USER_PID" 2>/dev/null; then
    kill "$USER_PID" 2>/dev/null || true
    wait "$USER_PID" 2>/dev/null || true
  fi
  if [[ -n "${GROUP_PID:-}" ]] && kill -0 "$GROUP_PID" 2>/dev/null; then
    kill "$GROUP_PID" 2>/dev/null || true
    wait "$GROUP_PID" 2>/dev/null || true
  fi
  if [[ -n "${GAME_PID:-}" ]] && kill -0 "$GAME_PID" 2>/dev/null; then
    kill "$GAME_PID" 2>/dev/null || true
    wait "$GAME_PID" 2>/dev/null || true
  fi
  if [[ -n "${LOGIC_PID:-}" ]] && kill -0 "$LOGIC_PID" 2>/dev/null; then
    kill "$LOGIC_PID" 2>/dev/null || true
    wait "$LOGIC_PID" 2>/dev/null || true
  fi
  echo "[info] Done"
}

trap shutdown EXIT INT TERM

start_redis

if [[ ! -x "$LOGIC_DIR/mvnw" || ! -x "$GAME_DIR/mvnw" || ! -x "$GROUP_DIR/mvnw" || ! -x "$USER_DIR/mvnw" || ! -x "$NOTIFICATION_DIR/mvnw" || ! -x "$GATEWAY_DIR/mvnw" ]]; then
  echo "[error] mvnw not found or not executable in service directories" >&2
  exit 1
fi

build_service "logic-service" "$LOGIC_DIR"
build_service "game-service" "$GAME_DIR"
build_service "group-service" "$GROUP_DIR"
build_service "user-service" "$USER_DIR"
build_service "notification-service" "$NOTIFICATION_DIR"
build_service "api-gateway" "$GATEWAY_DIR"

echo "[info] Starting logic-service on :9002"
(
  cd "$LOGIC_DIR"
  ./mvnw spring-boot:run
) &
LOGIC_PID=$!
wait_for_http "logic-service" "http://localhost:9002/"

echo "[info] Starting user-service on :9004"
(
  cd "$USER_DIR"
  ./mvnw spring-boot:run
) &
USER_PID=$!
wait_for_http "user-service" "http://localhost:9004/.well-known/jwks.json"

echo "[info] Starting notification-service on :9005"
(
  cd "$NOTIFICATION_DIR"
  ./mvnw spring-boot:run
) &
NOTIFICATION_PID=$!
wait_for_http "notification-service" "http://localhost:9005/"

echo "[info] Starting game-service on :9001 (Redis + logic-service enabled)"
(
  cd "$GAME_DIR"
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.persistence=redis --app.logic-service.enabled=true"
) &
GAME_PID=$!
wait_for_http "game-service" "http://localhost:9001/"

echo "[info] Starting group-service on :9003 (Redis + game-service enabled)"
(
  cd "$GROUP_DIR"
  ./mvnw spring-boot:run -Dspring-boot.run.arguments="--app.persistence=redis"
) &
GROUP_PID=$!
wait_for_http "group-service" "http://localhost:9003/"

echo "[info] Starting api-gateway on :9000"
(
  cd "$GATEWAY_DIR"
  ./mvnw spring-boot:run
) &
GATEWAY_PID=$!
wait_for_http "api-gateway" "http://localhost:9000/actuator/health"

echo "[info] logic-service PID: $LOGIC_PID"
echo "[info] game-service PID:  $GAME_PID"
echo "[info] group-service PID: $GROUP_PID"
echo "[info] user-service PID:  $USER_PID"
echo "[info] notification PID:  $NOTIFICATION_PID"
echo "[info] api-gateway PID:   $GATEWAY_PID"
echo "[info] Press Ctrl+C to stop all services"

wait "$LOGIC_PID" "$GAME_PID" "$GROUP_PID" "$USER_PID" "$NOTIFICATION_PID" "$GATEWAY_PID"
