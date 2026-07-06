package org.example.game;

import org.example.game.entities.Move;
import org.example.game.entities.Piece;
import org.example.game.entities.Position;
import org.example.game.entities.Team;

import java.util.List;

public interface Game {

    Move move(String from, String to);

    Move move(Position from, Position to);

    Move undo();

    List<Move> getLegalMoves(Position position);

    boolean isGameOver();

    Team winner();

    Team currentTurn();


    boolean isKingInCheck(Team team);

    boolean isCheckMate(Team team);

    boolean isDraw();
    Piece getPiece(Position position);


}
