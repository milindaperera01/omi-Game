package com.example.logicservice.service;

import com.example.logicservice.dto.*;
import com.example.logicservice.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MoveStrategy {

    private final RulesHelper rulesHelper;

    public MoveStrategy(RulesHelper rulesHelper) {
        this.rulesHelper = rulesHelper;
    }

    public ChooseMoveResponse chooseMove(ChooseMoveRequest request) {
        rulesHelper.validateUniqueCardsAtLeastOne(request.getHand(), "hand");

        if (request.getCurrentTrick().size() > 3) {
            throw new BadRequestException("currentTrick size cannot exceed 3");
        }

        Suit leadSuit = rulesHelper.leadSuit(request.getCurrentTrick());
        List<CardDto> legal = rulesHelper.legalMoves(request.getHand(), leadSuit);

        MoveContext context = buildContext(request);
        CardDto chosen;
        String reason;

        if (request.getCurrentTrick().isEmpty()) {
            chosen = chooseLeadCard(request.getHand(), request.getTrumpSuit(), context);
            reason = "Bot is leading this trick with history-aware lead";
        } else if (canFollowSuit(request.getHand(), leadSuit)) {
            chosen = chooseWhenFollowingSuit(legal, request.getCurrentTrick(), leadSuit, request.getTrumpSuit(), context);
            reason = "Bot followed suit with teammate-aware decision";
        } else {
            chosen = chooseWhenCannotFollow(request.getHand(), request.getCurrentTrick(), request.getTrumpSuit(), leadSuit, context);
            reason = "Bot could not follow suit and used trump/dump heuristic";
        }

        validateChosenCard(request.getHand(), leadSuit, chosen);

        ChooseMoveResponse response = new ChooseMoveResponse();
        response.setGameId(request.getGameId());
        response.setBotPlayerId(request.getBotPlayerId());
        response.setChosenCard(chosen);
        response.setReason(reason);
        return response;
    }

    private CardDto chooseLeadCard(List<CardDto> hand, Suit trumpSuit, MoveContext context) {
        List<CardDto> nonTrump = hand.stream().filter(c -> c.getSuit() != trumpSuit).toList();
        Map<Suit, List<CardDto>> grouped = new EnumMap<>(Suit.class);

        for (CardDto card : nonTrump) {
            grouped.computeIfAbsent(card.getSuit(), ignored -> new ArrayList<>()).add(card);
        }

        List<CardDto> preferredGroup = grouped.entrySet().stream()
                .sorted((a, b) -> {
                    int sizeCompare = Integer.compare(a.getValue().size(), b.getValue().size());
                    if (sizeCompare != 0) {
                        return sizeCompare;
                    }
                    // Prefer suits where opponents are likely void less often.
                    int safetyA = context.opponentsVoidCountBySuit.getOrDefault(a.getKey(), 0);
                    int safetyB = context.opponentsVoidCountBySuit.getOrDefault(b.getKey(), 0);
                    if (safetyA != safetyB) {
                        return Integer.compare(safetyB, safetyA);
                    }
                    return Integer.compare(
                            rulesHelper.highestCard(a.getValue()).getRank().getStrength(),
                            rulesHelper.highestCard(b.getValue()).getRank().getStrength()
                    );
                })
                .map(Map.Entry::getValue)
                .filter(cards -> cards.size() >= 2)
                .findFirst()
                .orElse(List.of());

        if (!preferredGroup.isEmpty()) {
            return rulesHelper.highestCard(preferredGroup);
        }
        if (!nonTrump.isEmpty()) {
            return rulesHelper.highestCard(nonTrump);
        }
        return rulesHelper.lowestCard(hand);
    }

    private CardDto chooseWhenFollowingSuit(List<CardDto> legal,
                                            List<PlayedCardDto> trick,
                                            Suit leadSuit,
                                            Suit trumpSuit,
                                            MoveContext context) {
        CardDto currentWinner = rulesHelper.currentWinningCard(trick, trumpSuit).getCard();
        PlayedCardDto winnerPlay = rulesHelper.currentWinningCard(trick, trumpSuit);
        if (winnerPlay.getSeatIndex() == context.teammateSeatIndex) {
            return rulesHelper.lowestCard(legal);
        }

        List<CardDto> winningCards = legal.stream()
                .filter(card -> rulesHelper.cardBeats(card, currentWinner, leadSuit, trumpSuit))
                .toList();

        if (!winningCards.isEmpty()) {
            return rulesHelper.lowestCard(winningCards);
        }
        return rulesHelper.lowestCard(legal);
    }

    private CardDto chooseWhenCannotFollow(List<CardDto> hand,
                                           List<PlayedCardDto> trick,
                                           Suit trumpSuit,
                                           Suit leadSuit,
                                           MoveContext context) {
        PlayedCardDto currentWinnerPlay = rulesHelper.currentWinningCard(trick, trumpSuit);
        if (currentWinnerPlay.getSeatIndex() == context.teammateSeatIndex) {
            List<CardDto> nonTrumps = hand.stream().filter(c -> c.getSuit() != trumpSuit).toList();
            return nonTrumps.isEmpty() ? rulesHelper.lowestCard(hand) : rulesHelper.lowestCard(nonTrumps);
        }

        List<CardDto> trumpCards = hand.stream().filter(c -> c.getSuit() == trumpSuit).toList();
        if (trumpCards.isEmpty()) {
            return rulesHelper.lowestCard(hand);
        }

        CardDto currentWinner = rulesHelper.currentWinningCard(trick, trumpSuit).getCard();
        List<CardDto> winningTrumps = trumpCards.stream()
                .filter(card -> rulesHelper.cardBeats(card, currentWinner, leadSuit, trumpSuit))
                .toList();

        if (!winningTrumps.isEmpty()) {
            return rulesHelper.lowestCard(winningTrumps);
        }

        if (currentWinner.getSuit() != trumpSuit) {
            return rulesHelper.lowestCard(trumpCards);
        }

        return rulesHelper.lowestCard(hand);
    }

    private MoveContext buildContext(ChooseMoveRequest request) {
        MoveContext context = new MoveContext();
        context.botSeatIndex = request.getBotSeatIndex();
        context.teammateSeatIndex = request.getTeamInfo() != null
                ? request.getTeamInfo().getTeammateSeatIndex()
                : (request.getBotSeatIndex() + 2) % 4;
        context.opponentsVoidCountBySuit = estimateOpponentVoidBySuit(request, context.teammateSeatIndex);
        return context;
    }

    private Map<Suit, Integer> estimateOpponentVoidBySuit(ChooseMoveRequest request, int teammateSeatIndex) {
        Map<Suit, Integer> voidCounts = new EnumMap<>(Suit.class);
        for (Suit suit : Suit.values()) {
            voidCounts.put(suit, 0);
        }

        if (request.getTrickHistory() == null) {
            return voidCounts;
        }

        for (List<PlayedCardDto> trick : request.getTrickHistory()) {
            if (trick == null || trick.isEmpty()) {
                continue;
            }
            Suit lead = trick.getFirst().getCard().getSuit();
            for (PlayedCardDto play : trick) {
                if (play.getSeatIndex() == request.getBotSeatIndex() || play.getSeatIndex() == teammateSeatIndex) {
                    continue;
                }
                if (play.getCard().getSuit() != lead) {
                    voidCounts.put(lead, voidCounts.get(lead) + 1);
                }
            }
        }
        return voidCounts;
    }

    private boolean canFollowSuit(List<CardDto> hand, Suit leadSuit) {
        if (leadSuit == null) {
            return false;
        }
        return hand.stream().anyMatch(card -> card.getSuit() == leadSuit);
    }

    private void validateChosenCard(List<CardDto> hand, Suit leadSuit, CardDto chosen) {
        if (!hand.contains(chosen)) {
            throw new BadRequestException("Chosen card is not in hand");
        }

        if (leadSuit != null) {
            boolean hasLeadSuit = hand.stream().anyMatch(card -> card.getSuit() == leadSuit);
            if (hasLeadSuit && chosen.getSuit() != leadSuit) {
                throw new BadRequestException("Chosen card must follow lead suit");
            }
        }
    }

    private static class MoveContext {
        int botSeatIndex;
        int teammateSeatIndex;
        Map<Suit, Integer> opponentsVoidCountBySuit = Map.of();
    }
}
