package com.game.game_service.service;

import com.game.game_service.domain.GameState;
import com.game.game_service.domain.PlayerState;
import com.game.game_service.domain.TrickRecord;
import com.game.game_service.dto.GameView;
import com.game.game_service.dto.PlayerView;
import com.game.game_service.dto.TrickSummaryView;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class GameViewMapper {

    public GameView toView(GameState gameState, String viewerPlayerId) {
        GameView view = new GameView();
        view.setGameId(gameState.getGameId());
        view.setStatus(gameState.getStatus());
        view.setTrumpSuit(gameState.getTrumpSuit());
        view.setDealerPlayerId(gameState.getDealerPlayerId());
        view.setTrumpChooserPlayerId(gameState.getTrumpChooserPlayerId());
        view.setCurrentTurnPlayerId(gameState.getCurrentTurnPlayerId());
        view.setLeaderPlayerId(gameState.getLeaderPlayerId());
        view.setCurrentTrickNumber(gameState.getCurrentTrickNumber());
        view.setCurrentTrick(new ArrayList<>(gameState.getCurrentTrick()));
        view.setHandTricksRed(gameState.getHandTricksRed());
        view.setHandTricksBlue(gameState.getHandTricksBlue());
        view.setMatchHandsRed(gameState.getMatchHandsRed());
        view.setMatchHandsBlue(gameState.getMatchHandsBlue());

        List<PlayerView> players = gameState.getPlayers().stream()
                .sorted(Comparator.comparingInt(PlayerState::getSeatIndex))
                .map(player -> toPlayerView(player, viewerPlayerId))
                .toList();
        view.setPlayers(players);

        List<TrickSummaryView> history = gameState.getTrickHistory().stream()
                .map(this::toTrickSummary)
                .toList();
        view.setTrickHistory(history);

        return view;
    }

    private PlayerView toPlayerView(PlayerState player, String viewerPlayerId) {
        PlayerView view = new PlayerView();
        view.setPlayerId(player.getPlayerId());
        view.setDisplayName(player.getDisplayName());
        view.setSeatIndex(player.getSeatIndex());
        view.setPlayerType(player.getPlayerType());
        view.setTeam(player.getTeam());

        if (viewerPlayerId != null && viewerPlayerId.equals(player.getPlayerId())) {
            view.setHand(new ArrayList<>(player.getHand()));
            view.setHandCount(player.getHand().size());
        } else {
            view.setHand(List.of());
            view.setHandCount(player.getHand().size());
        }
        return view;
    }

    private TrickSummaryView toTrickSummary(TrickRecord record) {
        TrickSummaryView view = new TrickSummaryView();
        view.setTrickNumber(record.getTrickNumber());
        view.setWinnerPlayerId(record.getWinnerPlayerId());
        view.setWinnerTeam(record.getWinnerTeam());
        view.setPlayedCards(new ArrayList<>(record.getPlayedCards()));
        return view;
    }
}
