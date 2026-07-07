package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.Move;
import org.example.game.entities.MoveType;
import org.example.game.entities.PieceType;
import org.example.game.entities.Team;
import org.example.game.exceptions.GameOverException;

import java.util.*;

public class BotAlphaBeta2 implements Bot {

    private Team team;
    private ChessGame game;
    private Random rand;
    private int nbMovesPlanned;
    private long seed = System.nanoTime();
    private boolean isPlayerMax;

    private record HashEntry(int score, int depth, int flag) {}

    private static final int EXACT = 0;
    private static final int LOWER_BOUND = 1;
    private static final int UPPER_BOUND = 2;

    private Map<Long, HashEntry> transpositionTable = new HashMap<>();

    public BotAlphaBeta2(ChessGame game, Team team, int nbMovesPlanned) {
        this.game = game;
        this.team = team;
        rand = new Random(seed);
        this.nbMovesPlanned = nbMovesPlanned;
        isPlayerMax = Team.WHITE.equals(team);
    }

    public BotAlphaBeta2(ChessGame game, Team team, int nbMovesPlanned, long seed) {
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

        long rootHash = game.getCurrentHash();
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

            if (isPlayerMax) {
                if (score > bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                alpha = Math.max(alpha, bestScore);
            } else {
                if (score < bestScore) {
                    bestScore = score;
                    bestMove = move;
                }
                beta = Math.min(beta, bestScore);
            }
        }

        assert bestMove != null;

        transpositionTable.put(rootHash, new HashEntry(bestScore, nbMovesPlanned, EXACT));

        if (bestMove.moveType() == MoveType.PROMOTION) {
            game.movePromotion(bestMove.from(), bestMove.to(), PieceType.QUEEN);
        } else {
            game.move(bestMove.from(), bestMove.to());
        }
    }

    private int miniMax(int lvl, boolean isPlayerMax, int alpha, int beta) {
        if (lvl == 0 || game.isGameOver()) {
            return evaluate(lvl);
        }

        long currentHash = game.getCurrentHash();

        if (transpositionTable.containsKey(currentHash)) {
            HashEntry entry = transpositionTable.get(currentHash);

            if (entry.depth >= lvl) {
                if (entry.flag == EXACT) return entry.score();
                if (entry.flag == LOWER_BOUND) alpha = Math.max(alpha, entry.score());
                if (entry.flag == UPPER_BOUND) beta = Math.min(beta, entry.score());
                if (alpha >= beta) return entry.score();
            }
        }

        Team currentTeam = isPlayerMax ? Team.WHITE : Team.BLACK;
        List<Move> allMoves = game.getAllLegalMoves(currentTeam);

        int originalAlpha = alpha;
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

        int flag = EXACT;
        if (best <= originalAlpha) flag = UPPER_BOUND;
        else if (best >= beta) flag = LOWER_BOUND;

        transpositionTable.put(currentHash, new HashEntry(best, lvl, flag));

        return best;
    }

    public int evaluate(int depth) {
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