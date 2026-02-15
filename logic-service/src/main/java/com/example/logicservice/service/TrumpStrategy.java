package com.example.logicservice.service;

import com.example.logicservice.dto.*;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TrumpStrategy {

    private static final Map<Rank, Integer> SCORE_BY_RANK = Map.of(
            Rank.ACE, 6,
            Rank.KING, 5,
            Rank.QUEEN, 4,
            Rank.JACK, 3,
            Rank.TEN, 2,
            Rank.NINE, 1,
            Rank.EIGHT, 0,
            Rank.SEVEN, 0
    );

    private static final List<Suit> SUIT_TIE_ORDER = List.of(
            Suit.HEARTS,
            Suit.SPADES,
            Suit.DIAMONDS,
            Suit.CLUBS
    );

    private final RulesHelper rulesHelper;

    public TrumpStrategy(RulesHelper rulesHelper) {
        this.rulesHelper = rulesHelper;
    }

    public ChooseTrumpResponse chooseTrump(ChooseTrumpRequest request) {
        rulesHelper.validateUniqueCards(request.getFirstFourCards(), "firstFourCards", 4);

        Map<Suit, Integer> suitScores = new EnumMap<>(Suit.class);
        Map<Suit, Integer> suitCounts = new EnumMap<>(Suit.class);
        Map<Suit, Integer> highestRank = new EnumMap<>(Suit.class);

        for (Suit suit : Suit.values()) {
            suitScores.put(suit, 0);
            suitCounts.put(suit, 0);
            highestRank.put(suit, 0);
        }

        for (CardDto card : request.getFirstFourCards()) {
            Suit suit = card.getSuit();
            int nextCount = suitCounts.get(suit) + 1;
            suitCounts.put(suit, nextCount);
            suitScores.put(suit, suitScores.get(suit) + SCORE_BY_RANK.get(card.getRank()));
            highestRank.put(suit, Math.max(highestRank.get(suit), card.getRank().getStrength()));
        }

        for (Map.Entry<Suit, Integer> entry : suitCounts.entrySet()) {
            if (entry.getValue() >= 2) {
                suitScores.put(entry.getKey(), suitScores.get(entry.getKey()) + 2);
            }
        }

        Suit chosen = Arrays.stream(Suit.values())
                .max((a, b) -> compareSuits(a, b, suitScores, highestRank))
                .orElse(Suit.SPADES);

        ChooseTrumpResponse response = new ChooseTrumpResponse();
        response.setBotPlayerId(request.getBotPlayerId());
        response.setChosenSuit(chosen);
        response.setReason("Selected by suit score heuristic");
        return response;
    }

    private int compareSuits(Suit a, Suit b, Map<Suit, Integer> suitScores, Map<Suit, Integer> highestRank) {
        int scoreCompare = Integer.compare(suitScores.get(a), suitScores.get(b));
        if (scoreCompare != 0) {
            return scoreCompare;
        }

        int highestRankCompare = Integer.compare(highestRank.get(a), highestRank.get(b));
        if (highestRankCompare != 0) {
            return highestRankCompare;
        }

        return Integer.compare(SUIT_TIE_ORDER.indexOf(b), SUIT_TIE_ORDER.indexOf(a));
    }
}
