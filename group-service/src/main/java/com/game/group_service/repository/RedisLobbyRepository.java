package com.game.group_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.game.group_service.model.LobbyState;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(name = "app.persistence", havingValue = "redis")
public class RedisLobbyRepository implements LobbyRepository {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisLobbyRepository(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public LobbyState save(LobbyState state) {
        try {
            redisTemplate.opsForValue().set(key(state.getLobbyId()), objectMapper.writeValueAsString(state));
            return state;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize lobby", ex);
        }
    }

    @Override
    public Optional<LobbyState> findById(UUID lobbyId) {
        String raw = redisTemplate.opsForValue().get(key(lobbyId));
        if (raw == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, LobbyState.class));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize lobby", ex);
        }
    }

    private String key(UUID lobbyId) {
        return "lobby:" + lobbyId;
    }
}
