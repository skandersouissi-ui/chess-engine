package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.*;
import org.example.game.exceptions.GameOverException;
import org.example.game.exceptions.IllegalMoveException;

import java.util.List;
import java.util.Random;

public class BotDumb implements Bot {

    private Team team;
    private ChessGame game;
    private Random rand;
    private long seed=System.nanoTime();

    public BotDumb(ChessGame game, Team team) {
        this.game = game;
        this.team = team;
        rand = new Random(seed);
    }

    public BotDumb(ChessGame game, Team team, long seed) {
        this.game = game;
        this.team = team;
        this.seed = seed;
        rand = new Random(seed);
    }


    public void play() throws GameOverException {

        List<Piece> pieces = game.getPieces(team);

        List<Move> allMoves = pieces.stream()
                .flatMap(p -> game.getLegalMoves(p.getPosition()).stream())
                .toList();

        if (allMoves.isEmpty()) {
            throw new GameOverException("game over");
        }

        Move move = allMoves.get(rand.nextInt(allMoves.size()));

        if (move.moveType() == MoveType.PROMOTION) {
            game.movePromotion(move.from(), move.to(), PieceType.QUEEN);
        } else {
            game.move(move.from(), move.to());
        }

    }

    public long getSeed(){
        return seed;
    }
}
