package org.example.game.ruleset;

import org.example.game.gamestate.Board;
import org.example.game.entities.*;
import org.example.game.gamestate.GameState;

import java.util.*;

public class MoveGenerator {

    private final Board board;
    private final GameState gameState;

    public MoveGenerator(GameState gameState) {
        this.board = gameState.getBoard();
        this.gameState = gameState;
    }

    public List<Move> getAllMoves(Piece piece){
        List<Move> moves = getMoves(piece);

        if(piece.getType() == PieceType.KING){
            addCastling(moves,piece,Direction.LEFT);
            addCastling(moves,piece,Direction.RIGHT);
        }
        if(piece.getType() == PieceType.PAWN){
            addEnPassant(moves,piece);
        }
        return moves;
    }

    public List<Move> getMoves(Piece piece){

        return switch(piece.getType()) {
            case PAWN -> hasPawnMoves(piece);
            case ROOK -> hasRookMoves(piece);
            case BISHOP -> hasBishopMoves(piece);
            case KNIGHT -> hasKnightMoves(piece);
            case KING -> hasKingMoves(piece);
            case QUEEN -> {
                List<Move> moves = new ArrayList<>(hasRookMoves(piece));
                moves.addAll(hasBishopMoves(piece));
                yield moves;
            }
        };
    }


    private List<Move> hasBishopMoves(Piece piece){

        List<Move> positions = new ArrayList<>();

        for(Direction d:Direction.getDiagDirections()){
            positions.addAll(fillPositionList(piece, d));
        }

        return positions;
    }

    private List<Move> hasRookMoves(Piece piece){

        List<Move> positions = new ArrayList<>();

        for(Direction d:Direction.getNonDiagDirections()){
            positions.addAll(fillPositionList(piece, d));
        }

        return positions;
    }

    private List<Move> hasKnightMoves(Piece piece) {

        Position p = piece.getPosition();
        List<Move> positions = new ArrayList<>();

        for (Direction d : Direction.getNonDiagDirections()) {

            Optional<Position> forward2 = p.getOther(d, 2);

            if (forward2.isEmpty()) {
                continue;
            }

            Position base = forward2.get();

            Optional<Position> turnA = base.getOther(d.nextClockwise().nextClockwise(), 1);
            Optional<Position> turnB = base.getOther(d.nextCounterClockwise().nextCounterClockwise(), 1);

            addKnightMoveIfValid(turnA, piece, positions);
            addKnightMoveIfValid(turnB, piece, positions);
        }

        return new ArrayList<>(positions);
    }

    private void addKnightMoveIfValid(Optional<Position> pos, Piece piece,List<Move> positions) {

        if (pos.isEmpty()) return;

        if(!board.hasPiece(pos.get())){
            positions.add(Move.normal(piece.getPosition(),pos.get(),piece));
            return;
        }
        Piece target = board.getPiece(pos.get());
        if(target.getTeam() != piece.getTeam()){
            positions.add(Move.capture(piece.getPosition(),pos.get(),piece,target));
        }
    }

    private List<Move> hasKingMoves(Piece piece) {

        List<Move> positions = new ArrayList<>();

        for (Direction direction : Direction.values()) {

            piece.getPosition()
                    .getOther(direction, 1)
                    .ifPresent(pos -> {

                        Piece target = board.getPiece(pos);
                        if(target == null){
                            positions.add(Move.normal(piece.getPosition(),pos,piece));
                        }else{
                            if(target.getTeam() != piece.getTeam()){
                                positions.add(Move.capture(piece.getPosition(),pos,piece,target));
                            }
                        }

                    });
        }

        return positions;
    }


    public void addCastling(List<Move> list, Piece piece, Direction direction){

        if (!gameState.canCastle(piece.getTeam(), direction)) return;
        if (gameState.isKingInCheck(piece.getTeam())) return;

        Position kingPos = piece.getPosition();
        int distRook = direction == Direction.RIGHT ? 3 : 4;

        for (int n = 1; n <= 2; n++) {
            Position between = kingPos.getOther(direction, n).orElseThrow();
            if (board.hasPiece(between)) return;
            Move testMove = Move.normal(kingPos, between, piece);
            if (gameState.simulate(testMove, () -> gameState.isKingInCheck(piece.getTeam()))) {
                return;
            }
        }

        if (direction == Direction.LEFT) {
            Position thirdSquare = kingPos.getOther(direction, distRook - 1).orElseThrow();
            if (board.hasPiece(thirdSquare)) return;
        }

        Position pos = kingPos.getOther(direction, 2).orElseThrow();
        int distance = direction == Direction.LEFT ? 4 : 3;

        Position rookPosFrom = kingPos.getOther(direction, distance).orElseThrow();
        Position rookPosTo = kingPos.getOther(direction, 1).orElseThrow();

        Move move = Move.castling(kingPos, pos, piece, Move.normal(rookPosFrom, rookPosTo, piece));
        list.add(move);
    }



    public List<Move> hasPawnMoves(Piece piece){

        Direction toward = piece.getTeam() == Team.BLACK ? Direction.DOWN : Direction.UP;
        int dist = piece.hasMoved() ? 1 : 2;

        List<Move> positions = new ArrayList<>();

        for (Move m : fillPositionList(piece, toward, dist)) {
            positions.add(isPromotionRow(piece, m.to()) ? Move.promotion(m.from(), m.to(), piece, null, PieceType.QUEEN) : m);
        }

        piece.getPosition()
                .getOther(toward.nextCounterClockwise(), 1)
                .ifPresent(pos -> {
                    Piece target = board.getPiece(pos);
                    if (target != null && target.getTeam() != piece.getTeam()) {
                        positions.add(isPromotionRow(piece, pos)
                                ? Move.promotion(piece.getPosition(), pos, piece, target, PieceType.QUEEN)
                                : Move.capture(piece.getPosition(), pos, piece, target));
                    }
                });

        piece.getPosition()
                .getOther(toward.nextClockwise(), 1)
                .ifPresent(pos -> {
                    Piece target = board.getPiece(pos);
                    if (target != null && target.getTeam() != piece.getTeam()) {
                        positions.add(isPromotionRow(piece, pos)
                                ? Move.promotion(piece.getPosition(), pos, piece, target, PieceType.QUEEN)
                                : Move.capture(piece.getPosition(), pos, piece, target));
                    }
                });

        return positions;
    }

    private boolean isPromotionRow(Piece piece, Position to) {
        return (piece.getTeam() == Team.WHITE && to.row() == 7)
                || (piece.getTeam() == Team.BLACK && to.row() == 0);
    }

    public void addEnPassant(List<Move> list, Piece piece){

        Optional<Position> targetOpt = gameState.getEnPassantTarget();
        if (targetOpt.isEmpty()) return;

        Position posToAdd = targetOpt.get();
        Move lastMove = gameState.getLastMove();
        Piece opponentPawn = gameState.getBoard().getPiece(lastMove.to());

        if (opponentPawn.getTeam().equals(piece.getTeam())) return;

        Optional<Direction> vect = piece.getPosition().getDirection(opponentPawn.getPosition());
        if (vect.isEmpty()) return;
        if (vect.get() != Direction.LEFT && vect.get() != Direction.RIGHT) return;

        int dist = piece.getPosition().getDistance(opponentPawn.getPosition());
        if (dist != 1) return;

        if (piece.getTeam() == Team.WHITE && piece.getPosition().row() != 4) return;
        if (piece.getTeam() == Team.BLACK && piece.getPosition().row() != 3) return;

        if (gameState.getBoard().hasPiece(posToAdd)) return;

        list.add(Move.enPassant(piece.getPosition(), posToAdd, piece, opponentPawn));
    }

    public List<Move> fillPositionList(Piece piece, Direction direction, int dist) {

        List<Move> positions = new ArrayList<>();

        for(int n = 1; n <= dist; n++) {

            Optional<Position> move = piece.getPosition().getOther(direction, n);

            if (move.isEmpty() || board.getPiece(move.get()) != null) {
                break;
            }

            positions.add(Move.normal(piece.getPosition(),move.get(),piece));
        }

        return positions;
    }

    public List<Move> fillPositionList(Piece piece,Direction direction){
        List<Move> positions = new ArrayList<>();

        Position curr = piece.getPosition();

        while(true){
            Optional<Position> next = curr.getOther(direction,1);

            if(next.isEmpty()){
                break;
            }

            if(board.getPiece(next.get())!=null){
                if (!board.getPiece(next.get()).getTeam().equals(piece.getTeam())) {
                    curr = next.get();
                    Piece captured = board.getPiece(curr);
                    positions.add(Move.capture(piece.getPosition(),curr,piece,captured));
                }
                break;
            }

            curr = next.get();
            positions.add(Move.normal(piece.getPosition(),curr,piece));
        }

        return positions;
    }

    public List<Position> getAttackedSquares(Piece piece) {

        if (piece.getType() != PieceType.PAWN) {
            return getMoves(piece).stream().map(Move::to).toList();
        }

        List<Position> attacks = new ArrayList<>();

        Direction toward = piece.getTeam() == Team.BLACK
                ? Direction.DOWN
                : Direction.UP;

        piece.getPosition()
                .getOther(toward.nextCounterClockwise(), 1)
                .ifPresent(attacks::add);

        piece.getPosition()
                .getOther(toward.nextClockwise(), 1)
                .ifPresent(attacks::add);

        return attacks;
    }

    public boolean computeIfKingInCheck(Team team){

        Position kingPos = team.equals(Team.BLACK) ? gameState.getBlackKing() : gameState.getWhiteKing();

        for(Piece piece:board.getPieces(team.getOpponent())){
            if(getAttackedSquares(piece).contains(kingPos)){
                return true;
            }
        }
        return false;
    }
}
