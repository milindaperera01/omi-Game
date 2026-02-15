package com.game.game_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.game_service.domain.GameState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "app.persistence", havingValue = "redis")
public class RedisGameStateRepository implements GameStateRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisGameStateRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public GameState save(GameState gameState) {
        String key = key(gameState.getGameId());
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(gameState));
            return gameState;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize game state", e);
        }
    }

    @Override
    public Optional<GameState> findById(UUID gameId) {
        String raw = redisTemplate.opsForValue().get(key(gameId));
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, GameState.class));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize game state", e);
        }
    }

    @Override
    public void delete(UUID gameId) {
        redisTemplate.delete(key(gameId));
    }

    private String key(UUID gameId) {
        return "game:" + gameId;
    }
}
