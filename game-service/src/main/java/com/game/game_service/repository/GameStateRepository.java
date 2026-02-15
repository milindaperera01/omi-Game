package com.game.game_service.repository;

import com.game.game_service.domain.GameState;

import java.util.Optional;
import java.util.UUID;

public interface GameStateRepository {
    GameState save(GameState gameState);

    Optional<GameState> findById(UUID gameId);

    void delete(UUID gameId);
}
