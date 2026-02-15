package com.example.logicservice.dto;

public enum Rank {
    ACE(8),
    KING(7),
    QUEEN(6),
    JACK(5),
    TEN(4),
    NINE(3),
    EIGHT(2),
    SEVEN(1);

    private final int strength;

    Rank(int strength) {
        this.strength = strength;
    }

    public int getStrength() {
        return strength;
    }
}
