package org.example.game.exceptions;

public class GameOverException extends IllegalStateException {
    public GameOverException(String message) {
        super(message);
    }
}
