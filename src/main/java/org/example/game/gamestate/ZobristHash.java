package org.example.game.gamestate;

import org.example.game.entities.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

public class ZobristHash {

    private final GameState gameState;

    private final long[][][] keyPos = new long[2][6][64];
    private final long[] keyPosCastling = new long[4];
    private final long[] keyPosEnPassant = new long[8];
    private long zobristKey;

    private long currentHash = 0;

    private final Stack<Long> hashHistory = new Stack<>();
    private final Map<Long,Integer> zobristMap = new HashMap<>();

    public ZobristHash(GameState gameState) {
        this.gameState = gameState;
        init();
        computeHashFromScratch();
    }

    private void init(){
        Random rand = new Random(42);

        for(int i = 0; i < keyPos.length; i++){
            for(int j = 0; j < keyPos[i].length; j++){
                for(int k = 0; k < keyPos[i][j].length; k++){
                    keyPos[i][j][k] = rand.nextLong();
                }
            }
        }
        for(int i = 0; i < keyPosCastling.length; i++){
            keyPosCastling[i] = rand.nextLong();
        }
        for(int i = 0; i < keyPosEnPassant.length; i++){
            keyPosEnPassant[i] = rand.nextLong();
        }

        zobristKey = rand.nextLong();
    }

    public void computeHashFromScratch(){
        long hash = 0;
        for(Position pos : gameState.getBoard().getBoard().keySet()){
            if(gameState.getBoard().hasPiece(pos)){
                hash ^= getKey(pos);
            }
        }
        currentHash = hash;
        zobristMap.clear();
        zobristMap.put(currentHash, 1);
    }

    public long getKey(Position pos){
        Piece p = gameState.getBoard().getPiece(pos);
        int square = pos.row()*8 + pos.col();
        return keyPos[p.getTeam().ordinal()][p.getType().ordinal()][square];
    }

    public long keyFor(Team team, PieceType type, Position pos){
        int square = pos.row()*8 + pos.col();
        return keyPos[team.ordinal()][type.ordinal()][square];
    }

    public long getCurrentHash(){
        return currentHash;
    }

    public boolean isThreefoldRepetition(){
        return zobristMap.getOrDefault(currentHash, 0) >= 3;
    }

    public Move move(Move move){
        hashHistory.push(currentHash);

        boolean[] castlingBefore = snapshotCastlingRights();
        Position enPassantBefore = gameState.getEnPassantTarget().orElse(null);

        gameState.applyMove(move);

        Team team = move.moved().getTeam();
        PieceType type = move.moved().getType();

        switch (move.moveType()) {
            case NORMAL -> {
                currentHash ^= keyFor(team, type, move.from());
                currentHash ^= keyFor(team, type, move.to());
            }
            case CAPTURE -> {
                currentHash ^= keyFor(team, type, move.from());
                currentHash ^= keyFor(team, type, move.to());
                Piece captured = move.captured();
                currentHash ^= keyFor(captured.getTeam(), captured.getType(), move.to());
            }
            case EN_PASSANT -> {
                currentHash ^= keyFor(team, type, move.from());
                currentHash ^= keyFor(team, type, move.to());
                Piece captured = move.captured();
                currentHash ^= keyFor(captured.getTeam(), captured.getType(), captured.getPosition());
            }
            case CASTLING -> {
                currentHash ^= keyFor(team, type, move.from());
                currentHash ^= keyFor(team, type, move.to());
                Move rookMove = move.secondaryMove();
                Piece rook = rookMove.moved();
                currentHash ^= keyFor(rook.getTeam(), rook.getType(), rookMove.from());
                currentHash ^= keyFor(rook.getTeam(), rook.getType(), rookMove.to());
            }
            case PROMOTION -> {
                currentHash ^= keyFor(team, type, move.from());
                currentHash ^= keyFor(team, move.promotedTo(), move.to());
                if (move.captured() != null) {
                    Piece captured = move.captured();
                    currentHash ^= keyFor(captured.getTeam(), captured.getType(), move.to());
                }
            }
        }

        currentHash ^= zobristKey;

        applyCastlingDelta(castlingBefore);
        applyEnPassantDelta(enPassantBefore);

        zobristMap.merge(currentHash, 1, Integer::sum);
        return move;
    }

    private boolean[] snapshotCastlingRights() {
        return new boolean[] {
                gameState.canCastle(Team.WHITE, Direction.RIGHT),
                gameState.canCastle(Team.WHITE, Direction.LEFT),
                gameState.canCastle(Team.BLACK, Direction.RIGHT),
                gameState.canCastle(Team.BLACK, Direction.LEFT)
        };
    }

    private void applyCastlingDelta(boolean[] before) {
        boolean[] after = snapshotCastlingRights();
        for (int i = 0; i < 4; i++) {
            if (before[i] != after[i]) {
                currentHash ^= keyPosCastling[i];
            }
        }
    }

    private void applyEnPassantDelta(Position before) {
        Position after = gameState.getEnPassantTarget().orElse(null);

        if (before != null) currentHash ^= keyPosEnPassant[before.col()];
        if (after != null) currentHash ^= keyPosEnPassant[after.col()];
    }

    public void undo(){
        zobristMap.merge(currentHash, -1, Integer::sum);
        currentHash = hashHistory.pop();
    }

}