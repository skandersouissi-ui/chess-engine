package org.example.game.entities;

public enum Team {
    WHITE("w"),
    BLACK("b"),
    DRAW("");

    private final String algebraic;

    Team(String algebraic) {
        this.algebraic = algebraic;
    }

    public String toAlgebraic() {
        return algebraic;
    }

    public Team getOpponent(){
        return this == WHITE ? BLACK : WHITE;
    }
}