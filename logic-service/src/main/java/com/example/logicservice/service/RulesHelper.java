package com.example.logicservice.service;

import com.example.logicservice.dto.CardDto;
import com.example.logicservice.dto.PlayedCardDto;
import com.example.logicservice.dto.Suit;
import com.example.logicservice.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RulesHelper {

    public void validateUniqueCards(List<CardDto> cards, String fieldName, int expectedSize) {
        if (cards == null || cards.size() != expectedSize) {
            throw new BadRequestException(fieldName + " must contain exactly " + expectedSize + " cards");
        }
        Set<CardDto> unique = new HashSet<>(cards);
        if (unique.size() != cards.size()) {
            throw new BadRequestException(fieldName + " must contain unique cards");
        }
    }

    public void validateUniqueCardsAtLeastOne(List<CardDto> cards, String fieldName) {
        if (cards == null || cards.isEmpty()) {
            throw new BadRequestException(fieldName + " must contain at least 1 card");
        }
        Set<CardDto> unique = new HashSet<>(cards);
        if (unique.size() != cards.size()) {
            throw new BadRequestException(fieldName + " must contain unique cards");
        }
    }

    public Suit leadSuit(List<PlayedCardDto> trick) {
        if (trick == null || trick.isEmpty()) {
            return null;
        }
        return trick.getFirst().getCard().getSuit();
    }

    public List<CardDto> legalMoves(List<CardDto> hand, Suit leadSuit) {
        if (leadSuit == null) {
            return new ArrayList<>(hand);
        }
        List<CardDto> followSuitCards = hand.stream().filter(c -> c.getSuit() == leadSuit).toList();
        return followSuitCards.isEmpty() ? new ArrayList<>(hand) : new ArrayList<>(followSuitCards);
    }

    public PlayedCardDto currentWinningCard(List<PlayedCardDto> trick, Suit trumpSuit) {
        if (trick == null || trick.isEmpty()) {
            throw new BadRequestException("currentTrick must not be empty when evaluating winner");
        }

        Suit lead = leadSuit(trick);
        List<PlayedCardDto> trumps = trick.stream().filter(pc -> pc.getCard().getSuit() == trumpSuit).toList();
        List<PlayedCardDto> candidates = trumps.isEmpty()
                ? trick.stream().filter(pc -> pc.getCard().getSuit() == lead).toList()
                : trumps;

        return candidates.stream()
                .max(Comparator.comparingInt(pc -> pc.getCard().getRank().getStrength()))
                .orElseThrow();
    }

    public boolean cardBeats(CardDto challenger, CardDto currentWinner, Suit leadSuit, Suit trumpSuit) {
        if (challenger.getSuit() == currentWinner.getSuit()) {
            return challenger.getRank().getStrength() > currentWinner.getRank().getStrength();
        }

        boolean challengerTrump = challenger.getSuit() == trumpSuit;
        boolean winnerTrump = currentWinner.getSuit() == trumpSuit;

        if (challengerTrump && !winnerTrump) {
            return true;
        }
        if (!challengerTrump && winnerTrump) {
            return false;
        }

        return challenger.getSuit() == leadSuit && currentWinner.getSuit() != leadSuit;
    }

    public CardDto lowestCard(Collection<CardDto> cards) {
        return cards.stream()
                .min(Comparator.comparingInt(c -> c.getRank().getStrength()))
                .orElseThrow(() -> new BadRequestException("No candidate cards available"));
    }

    public CardDto highestCard(Collection<CardDto> cards) {
        return cards.stream()
                .max(Comparator.comparingInt(c -> c.getRank().getStrength()))
                .orElseThrow(() -> new BadRequestException("No candidate cards available"));
    }
}
