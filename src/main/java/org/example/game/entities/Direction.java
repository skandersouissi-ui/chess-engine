package org.example.game.entities;

import java.util.Set;

public enum Direction {

    UP,
    DIAG_UP_RIGHT,
    RIGHT,
    DIAG_DOWN_RIGHT,
    DOWN,
    DIAG_DOWN_LEFT,
    LEFT,
    DIAG_UP_LEFT;

    private static final Direction[] CLOCKWISE = values();

    public Direction nextClockwise() {
        return CLOCKWISE[(ordinal() + 1) % CLOCKWISE.length];
    }

    public Direction nextCounterClockwise() {
        return CLOCKWISE[(ordinal() + CLOCKWISE.length - 1) % CLOCKWISE.length];
    }

    public static Set<Direction> getNonDiagDirections(){
        return Set.of(UP, LEFT, DOWN, RIGHT);
    }

    public static Set<Direction> getDiagDirections(){
        return Set.of(DIAG_DOWN_LEFT, DIAG_DOWN_RIGHT, DIAG_UP_LEFT, DIAG_UP_RIGHT);
    }


}
