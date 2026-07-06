package org.example.game.exceptions;

public class IllegalMoveException extends IllegalArgumentException {
    public IllegalMoveException(String message) {
        super(message);
    }
}
