package com.example.logicservice.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public class CardDto {
    @NotNull
    private Suit suit;

    @NotNull
    private Rank rank;

    public CardDto() {
    }

    public CardDto(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CardDto cardDto)) {
            return false;
        }
        return suit == cardDto.suit && rank == cardDto.rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(suit, rank);
    }
}
