package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.Move;
import org.example.game.entities.MoveType;
import org.example.game.entities.PieceType;
import org.example.game.entities.Team;
import org.example.game.exceptions.GameOverException;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class BotAlphaBeta implements Bot{

    private Team team;
    private ChessGame game;
    private Random rand;
    private int nbMovesPlanned;
    private long seed = System.nanoTime();

    private boolean isPlayerMax;

    public BotAlphaBeta(ChessGame game, Team team,int nbMovesPlanned) {
        this.game = game;
        this.team = team;
        rand = new Random(seed);
        this.nbMovesPlanned = nbMovesPlanned;
        isPlayerMax = Team.WHITE.equals(team);
    }

    public BotAlphaBeta(ChessGame game, Team team,int nbMovesPlanned,long seed) {
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

        allMoves = allMoves.stream().sorted(Comparator.comparing(this::captureScore).reversed()).toList();

        Move bestMove = null;
        int bestScore = isPlayerMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (Move move : allMoves) {
            int currentAlpha = alpha;
            int currentBeta = beta;

            int score = game.getGameState().simulate(
                    move,
                    () -> miniMax(nbMovesPlanned - 1, !isPlayerMax, currentAlpha, currentBeta)
            );

            if (bestMove == null
                    || (isPlayerMax && score > bestScore)
                    || (!isPlayerMax && score < bestScore)) {
                bestScore = score;
                bestMove = move;
            }

            if (isPlayerMax) alpha = Math.max(alpha, bestScore);
            else beta = Math.min(beta, bestScore);

            if (alpha >= beta) break;
        }

        assert bestMove != null;

        if (bestMove.moveType() == MoveType.PROMOTION) {
            game.movePromotion(bestMove.from(), bestMove.to(), PieceType.QUEEN);
        } else {
            game.move(bestMove.from(), bestMove.to());
        }
    }

    private int miniMax(int lvl, boolean isPlayerMax, int alpha, int beta) {

        if (lvl == 0) {
            return evaluate(lvl);
        }

        Team team = isPlayerMax ? Team.WHITE : Team.BLACK;
        List<Move> allMoves = game.getAllLegalMoves(team);

        int best = isPlayerMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (Move move : allMoves) {

            int currentAlpha = alpha;
            int currentBeta = beta;

            int score = game.getGameState().simulate(
                    move,
                    () -> miniMax(lvl - 1, !isPlayerMax, currentAlpha, currentBeta)
            );

            if (isPlayerMax) {
                best = Math.max(best, score);
                alpha = Math.max(alpha, best);
            } else {
                best = Math.min(best, score);
                beta = Math.min(beta, best);
            }

            if (alpha >= beta) break;
        }

        return best;
    }

    public int evaluate(int depth){

        if (game.isCheckMate(Team.WHITE))
            return -100000 - depth;

        if (game.isCheckMate(Team.BLACK))
            return 100000 + depth;

        if (game.isDraw())
            return 0;

        int score = game.getGameState().getBoard().getPieces(Team.WHITE).stream()
                .mapToInt(p -> p.getType().getVal())
                .sum();

        int opponentScore = game.getGameState().getBoard().getPieces(Team.BLACK).stream()
                .mapToInt(p -> p.getType().getVal())
                .sum();

        return score - opponentScore;
    }

    private int captureScore(Move move) {
        if (move.moveType() != MoveType.CAPTURE && move.moveType() != MoveType.EN_PASSANT) return 0;
        int victimValue = move.captured().getType().getVal();
        int attackerValue = move.moved().getType().getVal();
        return victimValue * 10 - attackerValue;
    }



    @Override
    public long getSeed() {
        return seed;
    }
}
