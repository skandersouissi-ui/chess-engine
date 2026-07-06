package org.example.game.ruleset;

import org.example.game.entities.*;
import org.example.game.exceptions.IllegalMoveException;
import org.example.game.gamestate.GameState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RuleValidator {

    private final GameState gameState;
    private final MoveGenerator moveGenerator;

    public RuleValidator(GameState gameState,MoveGenerator moveGenerator) {
        this.gameState = gameState;
        this.moveGenerator = moveGenerator;
    }

    public List<Move> getLegalPositions(Piece piece) {

        List<Move> pseudoMoves = moveGenerator.getAllMoves(piece);

        boolean mustValidate = piece.getType() == PieceType.KING
                || gameState.isKingInCheck(piece.getTeam())
                || isPinned(piece);

        if (!mustValidate) {

            List<Move> result = new ArrayList<>();
            for (Move m : pseudoMoves) {
                if (m.moveType() == MoveType.EN_PASSANT
                        && gameState.leavesKingInCheck(m, piece.getTeam(),moveGenerator)) {
                    continue;
                }
                result.add(m);
            }
            return result;
        }

        List<Move> legalPositions = new ArrayList<>();
        for (Move m : pseudoMoves) {
            if (!gameState.leavesKingInCheck(m, piece.getTeam(),moveGenerator)) {
                legalPositions.add(m);
            }
        }
        return legalPositions;
    }

    public boolean hasLegalMoves(Team team){
        for(Piece piece: gameState.getBoard().getPieces(team)){
            if(!getLegalPositions(piece).isEmpty()){
                return true;
            }
        }
        return false;
    }

    public Move findAskedMove(Position from, Position to)throws IllegalMoveException{

        List<Move> legalMoves = getLegalMoves(from);
        Optional<Move> candidate = legalMoves.stream()
                .filter(e -> e.from().equals(from) && e.to().equals(to))
                .findFirst();

        if (candidate.isEmpty()) {
            throw new IllegalMoveException("illegal move " + from + " to " + to);
        }

        return candidate.get();
    }

    public List<Move> getLegalMoves(Position pos){
        if(!gameState.getBoard().hasPiece(pos)) {
            throw new IllegalArgumentException("Piece " + pos + " does not exist");
        }

        Piece piece = gameState.getBoard().getPiece(pos);

        return getLegalPositions(piece);
    }

    private boolean isPinned(Piece piece) {
        Position kingPos = piece.getTeam() == Team.WHITE
                ? gameState.getWhiteKing() : gameState.getBlackKing();

        Optional<Direction> towardKing = piece.getPosition().getDirection(kingPos);
        if (towardKing.isEmpty()) return false;

        Direction dirToKing = towardKing.get();

        Position curr = piece.getPosition();
        while (true) {
            Optional<Position> next = curr.getOther(dirToKing, 1);
            if (next.isEmpty()) return false;
            curr = next.get();
            if (curr.equals(kingPos)) break;
            if (gameState.getBoard().hasPiece(curr)) return false;
        }

        Direction dirAway = dirToKing.nextClockwise().nextClockwise().nextClockwise().nextClockwise();
        curr = piece.getPosition();
        while (true) {
            Optional<Position> next = curr.getOther(dirAway, 1);
            if (next.isEmpty()) return false;
            curr = next.get();
            if (!gameState.getBoard().hasPiece(curr)) continue;

            Piece attacker = gameState.getBoard().getPiece(curr);
            if (attacker.getTeam() == piece.getTeam()) return false;

            boolean diagonal = Direction.getDiagDirections().contains(dirAway);
            return attacker.getType() == PieceType.QUEEN
                    || (diagonal && attacker.getType() == PieceType.BISHOP)
                    || (!diagonal && attacker.getType() == PieceType.ROOK);
        }
    }

    public boolean hasNotEnoughPieces(){

        List<Piece> whitePieces = gameState.getBoard().getPieces(Team.WHITE);
        List<Piece> blackPieces = gameState.getBoard().getPieces(Team.BLACK);

        boolean whiteInsufficient = isKingAloneOrKingPlusMinor(whitePieces);
        boolean blackInsufficient = isKingAloneOrKingPlusMinor(blackPieces);

        if (!whiteInsufficient || !blackInsufficient) return false;

        Optional<Piece> whiteBishop = findBishop(whitePieces);
        Optional<Piece> blackBishop = findBishop(blackPieces);

        if (whiteBishop.isPresent() && blackBishop.isPresent()) {
            return gameState.isLightSquare(whiteBishop.get().getPosition())
                    == gameState.isLightSquare(blackBishop.get().getPosition());
        }

        return true;
    }

    private boolean isKingAloneOrKingPlusMinor(List<Piece> pieces){
        if (pieces.size() == 1) return true;

        return pieces.size() == 2 && pieces.stream()
                .anyMatch(p -> p.getType() == PieceType.BISHOP || p.getType() == PieceType.KNIGHT);
    }

    private Optional<Piece> findBishop(List<Piece> pieces){
        return pieces.stream().filter(p -> p.getType() == PieceType.BISHOP).findFirst();
    }

    public boolean isDraw50Moves(){
        return gameState.getNbMoves() >= 100;
    }

    public boolean isStaleMate(Team team) {
        return !gameState.isKingInCheck(team) && !hasLegalMoves(team);
    }


}
