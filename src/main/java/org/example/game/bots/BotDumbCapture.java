package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.*;
import org.example.game.exceptions.GameOverException;
import org.example.game.exceptions.IllegalMoveException;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BotDumbCapture implements Bot{

    private Team team;
    private ChessGame game;
    private Random rand;
    private long seed = System.nanoTime();

    public BotDumbCapture(ChessGame game, Team team) {
        this.game = game;
        this.team = team;
        rand = new Random(seed);
    }

    public BotDumbCapture(ChessGame game, Team team,long seed) {
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

        List<Move> captureMoves = allMoves.stream().filter(move -> move.moveType()== MoveType.CAPTURE).toList();

        List<Move> castlingMoves = allMoves.stream().filter(move -> move.moveType()==MoveType.CASTLING).toList();

        Move move;

        if(!castlingMoves.isEmpty()){
            move = castlingMoves.get(rand.nextInt(castlingMoves.size()));
        }else if(!captureMoves.isEmpty()){
            move = captureMoves.stream()
                    .max(Comparator.comparingInt(m -> m.captured().getType().getVal())).orElseThrow();
        }else{
            move = allMoves.get(rand.nextInt(allMoves.size()));

        }

        try {
            if (move.moveType() == MoveType.PROMOTION) {
                game.movePromotion(move.from(), move.to(), PieceType.QUEEN);
            } else {
                game.move(move.from(), move.to());
            }
        } catch (IllegalMoveException e) {
            play();
        }
    }

    public long getSeed(){
        return seed;
    }
}
