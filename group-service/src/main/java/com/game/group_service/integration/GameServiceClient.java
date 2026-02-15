package com.game.group_service.integration;

import com.game.group_service.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GameServiceClient {

    private final RestClient restClient;

    public GameServiceClient(@Value("${app.game-service.base-url:http://localhost:9001}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public String createGame(CreateGameRequest request) {
        try {
            CreateGameResponse response = restClient.post()
                    .uri("/api/games")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(CreateGameResponse.class);

            if (response == null || response.getGameId() == null) {
                throw new BadRequestException("game-service returned empty gameId");
            }
            return response.getGameId().toString();
        } catch (Exception ex) {
            throw new BadRequestException("Failed to create game in game-service: " + ex.getMessage());
        }
    }
}
