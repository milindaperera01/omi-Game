#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="$SCRIPT_DIR/docker-compose.yml"

if [[ ! -f "$COMPOSE_FILE" ]]; then
  echo "[error] docker-compose.yml not found at $SCRIPT_DIR" >&2
  exit 1
fi

cd "$SCRIPT_DIR"

if [[ "${1:-}" == "--volumes" || "${1:-}" == "-v" ]]; then
  echo "[info] Stopping and removing containers, network, and volumes"
  docker compose down --volumes --remove-orphans
else
  echo "[info] Stopping and removing containers and network"
  docker compose down --remove-orphans
fi

if docker ps --format '{{.Names}}' | rg -q '^omi-game-'; then
  echo "[warn] Some omi-game containers are still running"
  docker ps --format 'table {{.Names}}\t{{.Status}}' | rg '^omi-game-|^NAMES' || true
  exit 1
fi

echo "[info] All omi-game compose containers are stopped"
