package org.example.game.entities;

import java.util.Objects;
import java.util.Optional;

public class Position {

    private final int col;
    private final int row;

    public Position(int col, int row) {
        if (col < 0 || col > 7 || row < 0 || row > 7) {
            throw new IllegalArgumentException("Position invalide");
        }
        this.col = col;
        this.row = row;
    }

    public Position(char col,int row){
        this(col-'a',row);
    }

    public static Position of(String alg) {
        int col = alg.charAt(0) - 'a';
        int row = alg.charAt(1) - '1';
        return new Position(col, row);
    }

    public String toAlgebraic() {
        return "" + (char) ('a' + col) + (row + 1);
    }

    public int col() {
        return col;
    }

    public char colChar(){
        return (char) ('a' + col);
    }

    public int row() {
        return row;
    }

    public Optional<Position> getOther(Direction direction, int distance) {

        int newCol = col;
        int newRow = row;

        switch (direction) {
            case UP -> newRow += distance;
            case DOWN -> newRow -= distance;
            case LEFT -> newCol -= distance;
            case RIGHT -> newCol += distance;
            case DIAG_UP_LEFT -> {
                newCol -= distance;
                newRow += distance;
            }
            case DIAG_UP_RIGHT -> {
                newCol += distance;
                newRow += distance;
            }
            case DIAG_DOWN_LEFT -> {
                newCol -= distance;
                newRow -= distance;
            }
            case DIAG_DOWN_RIGHT -> {
                newCol += distance;
                newRow -= distance;
            }
        }

        if (newCol < 0 || newCol > 7 || newRow < 0 || newRow > 7) {
            return Optional.empty();
        }

        return Optional.of(new Position(newCol, newRow));
    }

    public int getDistance(Position other) {
        return Math.max(
                Math.abs(other.col - this.col),
                Math.abs(other.row - this.row)
        );
    }

    public Optional<Direction> getDirection(Position other) {

        int dx = other.col - this.col;
        int dy = other.row - this.row;

        if (dx == 0 && dy == 0) return Optional.empty();

        if (dx == 0) {
            return Optional.of(dy > 0 ? Direction.UP : Direction.DOWN);
        }

        if (dy == 0) {
            return Optional.of(dx > 0 ? Direction.RIGHT : Direction.LEFT);
        }

        if (Math.abs(dx) == Math.abs(dy)) {
            if (dx > 0 && dy > 0) return Optional.of(Direction.DIAG_UP_RIGHT);
            if (dx < 0 && dy > 0) return Optional.of(Direction.DIAG_UP_LEFT);
            if (dx > 0 && dy < 0) return Optional.of(Direction.DIAG_DOWN_RIGHT);
            if (dx < 0 && dy < 0) return Optional.of(Direction.DIAG_DOWN_LEFT);
        }

        return Optional.empty();
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;
        Position p = (Position) o;
        return col == p.col && row == p.row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(col, row);
    }
}