package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.Move;
import org.example.game.entities.MoveType;
import org.example.game.entities.PieceType;
import org.example.game.entities.Team;
import org.example.game.exceptions.GameOverException;

import java.util.List;
import java.util.Random;

public class BotMiniMax implements Bot{

    private Team team;
    private ChessGame game;
    private Random rand;
    private int nbMovesPlanned;
    private long seed = System.nanoTime();

    private boolean isPlayerMax;

    public BotMiniMax(ChessGame game, Team team,int nbMovesPlanned) {
        this.game = game;
        this.team = team;
        rand = new Random(seed);
        this.nbMovesPlanned = nbMovesPlanned;
        isPlayerMax = Team.WHITE.equals(team);
    }

    public BotMiniMax(ChessGame game, Team team,int nbMovesPlanned,long seed) {
        this.game = game;
        this.team = team;
        this.seed = seed;
        this.nbMovesPlanned = nbMovesPlanned;
        rand = new Random(seed);
    }

    @Override
    public void play() throws GameOverException {

        if (game.isGameOver()) {
            throw new GameOverException("game over");
        }

        List<Move> allMoves = game.getAllLegalMoves(team);

        Move bestMove = null;
        int bestScore = isPlayerMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {

            int score = game.getGameState().simulate(
                    move,
                    bestScore,
                    b -> miniMax(nbMovesPlanned-1, !isPlayerMax)
            );

            if (bestMove == null
                    || (isPlayerMax && score > bestScore)
                    || (!isPlayerMax && score < bestScore)) {

                bestScore = score;
                bestMove = move;
            }
        }

        assert bestMove != null;

        if (bestMove.moveType() == MoveType.PROMOTION) {
            game.movePromotion(bestMove.from(), bestMove.to(), PieceType.QUEEN);
        } else {
            game.move(bestMove.from(), bestMove.to());
        }
    }

    private int miniMax(int lvl, boolean isPlayerMax) {

        if (lvl == 0 || game.isGameOver()) {
            return evaluate(lvl);
        }

        Team team = isPlayerMax ? Team.WHITE : Team.BLACK;
        List<Move> allMoves = game.getAllLegalMoves(team);

        int best = isPlayerMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {

            int score = game.getGameState().simulate(
                    move,
                    best,
                    b -> miniMax(lvl - 1, !isPlayerMax)
            );

            best = isPlayerMax
                    ? Math.max(best, score)
                    : Math.min(best, score);
        }

        return best;
    }

    public int evaluate(int depth){

//        if (game.isCheckMate(Team.WHITE))
//            return -100000 - depth;
//
//        if (game.isCheckMate(Team.BLACK))
//            return 100000 + depth;
//
//        if (game.isDraw())
//            return 0;

        int score = game.getGameState().getBoard().getPieces(Team.WHITE).stream()
                .mapToInt(p -> p.getType().getVal())
                .sum();

        int opponentScore = game.getGameState().getBoard().getPieces(Team.BLACK).stream()
                .mapToInt(p -> p.getType().getVal())
                .sum();

        return score - opponentScore;
    }

    @Override
    public long getSeed() {
        return seed;
    }
}
