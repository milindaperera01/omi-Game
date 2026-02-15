package com.game.game_service.integration;

import com.game.game_service.domain.Card;
import com.game.game_service.domain.PlayedCard;
import com.game.game_service.domain.Suit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class LogicServiceClient {
    private static final Logger log = LoggerFactory.getLogger(LogicServiceClient.class);

    private final RestClient restClient;
    private final boolean enabled;

    public LogicServiceClient(
            @Value("${app.logic-service.enabled:false}") boolean enabled,
            @Value("${app.logic-service.base-url:http://localhost:9002}") String baseUrl) {
        this.enabled = enabled;
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public Optional<Suit> chooseTrump(String playerId, int seatIndex, List<Card> cards) {
        if (!enabled) {
            return Optional.empty();
        }

        LogicTrumpRequest request = new LogicTrumpRequest();
        request.setBotPlayerId(playerId);
        request.setSeatIndex(seatIndex);
        request.setFirstFourCards(cards);

        try {
            LogicTrumpResponse response = restClient.post()
                    .uri("/api/logic/trump")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LogicTrumpResponse.class);

            return response == null ? Optional.empty() : Optional.ofNullable(response.getChosenSuit());
        } catch (Exception ex) {
            log.warn("Logic service trump selection failed for player {}: {}", playerId, ex.getMessage());
            return Optional.empty();
        }
    }

    public Optional<Card> chooseMove(String gameId,
                                     String botPlayerId,
                                     int botSeatIndex,
                                     Suit trumpSuit,
                                     List<Card> hand,
                                     List<PlayedCard> currentTrick,
                                     int trickNumber,
                                     List<List<PlayedCard>> trickHistory,
                                     Map<Integer, Integer> remainingCardsCountBySeat) {
        if (!enabled) {
            return Optional.empty();
        }

        LogicMoveRequest request = new LogicMoveRequest();
        request.setGameId(gameId);
        request.setBotPlayerId(botPlayerId);
        request.setBotSeatIndex(botSeatIndex);
        request.setTrumpSuit(trumpSuit);
        request.setHand(hand);
        request.setCurrentTrick(currentTrick);
        request.setTrickNumber(trickNumber);
        request.setTrickHistory(trickHistory);
        request.setRemainingCardsCountBySeat(remainingCardsCountBySeat);

        try {
            LogicMoveResponse response = restClient.post()
                    .uri("/api/logic/move")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(LogicMoveResponse.class);

            return response == null ? Optional.empty() : Optional.ofNullable(response.getChosenCard());
        } catch (Exception ex) {
            log.warn("Logic service move selection failed for player {}: {}", botPlayerId, ex.getMessage());
            return Optional.empty();
        }
    }
}
