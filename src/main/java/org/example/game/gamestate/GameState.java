package org.example.game.gamestate;

import org.example.game.entities.*;
import org.example.game.ruleset.MoveGenerator;

import java.util.Optional;
import java.util.Stack;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class GameState {

    private Board board;
    private final Stack<Move> moveStack;
    private Position whiteKing ;
    private Position blackKing;
    private Team currentTeam;

    private int nbMoves = 0;
    private final Stack<Integer> savedNbMoves;

    private boolean checkCacheb = false;
    private boolean checkCachew = false;

    private ZobristHash zobristHash;

    public GameState(){
        board = new Board();
        moveStack = new Stack<>();
        savedNbMoves = new Stack<>();
        whiteKing = Position.of("e1");
        blackKing = Position.of("e8");
        currentTeam = Team.WHITE;
        zobristHash = new ZobristHash(this);
    }

    public boolean isMoveStackEmpty(){
        return moveStack.isEmpty();
    }

    public boolean isKingInCheck(Team team){
        return team.equals(Team.BLACK)?checkCacheb:checkCachew;
    }

    public void changeCurrentTeam(){
        currentTeam = currentTeam.getOpponent();
    }

    public Move move(Move move){
        return zobristHash.move(move);
    }

    public Move applyMove(Move move){

        if (move.moveType()==MoveType.EN_PASSANT) {
            Position pawnDeleted = moveStack.peek().to();
            board.deletePiece(pawnDeleted);
            board.applyMove(move.from(), move.to());

        }else if (move.moveType()==MoveType.CASTLING) {

            board.applyMove(move.secondaryMove().from(),move.secondaryMove().to());
            board.applyMove(move.from(), move.to());

        }else if(move.moveType()==MoveType.PROMOTION){

            board.applyMove(move.from(), move.to());
            Piece promotionPiece = new Piece(move.promotedTo(),move.moved().getTeam(),move.to());
            board.addPiece(move.to(),promotionPiece);

        }else {
            board.applyMove(move.from(), move.to());
        }

        moveStack.push(move);

        if (move.from().equals(whiteKing)) {
            whiteKing = move.to();
        }
        if (move.from().equals(blackKing)) {
            blackKing = move.to();
        }

        checkNbMoves(move);

        return move;
    }

    public Move undo(){

        zobristHash.undo();

        Move move = moveStack.pop();

        if(move.moveType()==MoveType.EN_PASSANT){
            board.applyUndo(move.from(),move.to(),move.movedHadMoved(),null);
            board.addPiece(move.captured().getPosition(),move.captured());
        }else if (move.moveType()==MoveType.CASTLING){
            board.applyUndo(move.from(),move.to(),move.movedHadMoved(),move.captured());
            board.applyUndo(move.secondaryMove().from(),move.secondaryMove().to(),move.secondaryMove().movedHadMoved(),null);
        }
        else if(move.moveType()==MoveType.PROMOTION){
            Piece pawn = move.moved();
            pawn.setPosition(move.from());
            pawn.setMovedStatue(move.movedHadMoved());

            board.addPiece(move.from(), pawn);
            board.addPiece(move.to(), move.captured());

            if (move.captured() != null) {
                move.captured().setPosition(move.to());
            }
        }
        else{
            board.applyUndo(move.from(),move.to(),move.movedHadMoved(),move.captured());
        }

        if (move.moved().getType() == PieceType.KING) {
            if (move.moved().getTeam() == Team.WHITE) {
                whiteKing = move.from();
            } else {
                blackKing = move.from();
            }
        }

        undoNbMoves();

        return move;
    }

    public void checkNbMoves(Move move){
        savedNbMoves.push(nbMoves);
        if(move.moveType()==MoveType.EN_PASSANT || move.moveType()==MoveType.CAPTURE || move.moved().getType() == PieceType.PAWN){
            nbMoves = 0;
        }
        nbMoves++;
    }

    public void undoNbMoves(){
        nbMoves = savedNbMoves.pop();
    }

    public <T> T simulate(Move move, Supplier<T> action){
        move(move);
        try {
            return action.get();
        } finally {
            undo();
        }
    }

    public <T> T simulate(Move move, int n, IntFunction<T> action){
        move(move);
        try {
            return action.apply(n);
        } finally {
            undo();
        }
    }



    public boolean leavesKingInCheck(Move move, Team team, MoveGenerator moveGenerator){
        return simulate(move, () -> moveGenerator.computeIfKingInCheck(team));
    }

    public void refreshKingCheckStatut(MoveGenerator moveGenerator){
        checkCacheb = moveGenerator.computeIfKingInCheck(Team.BLACK);
        checkCachew = moveGenerator.computeIfKingInCheck(Team.WHITE);
    }


    public Move getLastMove(){
        return moveStack.peek();
    }

    public Position getWhiteKing(){
        return whiteKing;
    }
    public Position getBlackKing(){
        return blackKing;
    }
    public Team getCurrentTeam(){
        return currentTeam;
    }

    public int getNbMoves(){
        return nbMoves;
    }

    public boolean isLightSquare(Position pos) {
        return (pos.row() + pos.col()) % 2 == 0;
    }

    public Board getBoard(){
        return board;
    }

    public boolean canCastle(Team team, Direction direction) {

        Position kingPos = team == Team.WHITE ? whiteKing : blackKing;
        Piece king = board.getPiece(kingPos);

        if (king == null || king.hasMoved()) return false;

        int distRook = direction == Direction.RIGHT ? 3 : 4;
        Position rookPos = kingPos.getOther(direction, distRook).orElse(null);
        if (rookPos == null || !board.hasPiece(rookPos)) return false;

        Piece rook = board.getPiece(rookPos);
        if (rook.hasMoved()) return false;
        if (rook.getType() != PieceType.ROOK) return false;
        return rook.getTeam() == team;
    }

    public Optional<Position> getEnPassantTarget() {
        if (isMoveStackEmpty()) return Optional.empty();

        Move lastMove = getLastMove();
        Piece movedPiece = board.getPiece(lastMove.to());

        if (movedPiece == null || movedPiece.getType() != PieceType.PAWN) return Optional.empty();
        if (lastMove.from().getDistance(lastMove.to()) != 2) return Optional.empty();

        Direction cameFrom = lastMove.to().getDirection(lastMove.from()).orElse(null);
        if (cameFrom == null) return Optional.empty();

        return lastMove.to().getOther(cameFrom, 1);
    }

    public ZobristHash getZobristHash(){
        return zobristHash;
    }
}
