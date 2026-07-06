package org.example.game.entities;

public class Move {

    private final Position from;
    private final Position to;
    private final Piece moved;
    private final Piece captured;
    private final boolean movedHadMoved;
    private Move secondaryMove=null;
    private MoveType moveType;
    private PieceType promotedTo;

    private Move(
            Position from,
            Position to,
            Piece moved,
            boolean movedHadMoved,
            Piece captured,
            MoveType moveType
    ) {
        this.from = from;
        this.to = to;
        this.moved = moved;
        this.movedHadMoved = movedHadMoved;
        this.captured = captured;
        this.moveType = moveType;

    }

    public static Move normal(Position from, Position to, Piece moved) {
        return new Move(from,to,moved,moved.hasMoved(),null,MoveType.NORMAL);
    }

    public static Move capture(Position from, Position to, Piece moved, Piece captured) {
        return new Move(from,to,moved,moved.hasMoved(),captured,MoveType.CAPTURE);
    }

    public static Move castling(Position from, Position to,Piece moved, Move rookMove){
        Move m = new Move(from,to,moved,moved.hasMoved(),null,MoveType.CASTLING);
        m.setSecondaryMove(rookMove);
        return m;
    }

    public static Move enPassant(Position from, Position to, Piece moved, Piece captured) {
        return new Move(from,to,moved,moved.hasMoved(),captured,MoveType.EN_PASSANT);
    }

    public static Move promotion(Position from, Position to, Piece moved, Piece captured, PieceType promotedTo){
        Move m = new Move(from, to, moved, moved.hasMoved(), captured, MoveType.PROMOTION);
        m.setPromotedTo(promotedTo);
        return m;
    }

    public Move withPromotion(PieceType promotedTo){
        return Move.promotion(this.from,this.to,this.moved,this.captured,promotedTo);
    }


    public Position from() {
        return from;
    }

    public Position to() {
        return to;
    }

    public Piece moved() {
        return moved;
    }

    public Piece captured() {
        return captured;
    }

    public boolean movedHadMoved() {
        return movedHadMoved;
    }

    public Move secondaryMove() {
        return secondaryMove;
    }

    public void setSecondaryMove(Move secondaryMove) {
        this.secondaryMove = secondaryMove;
    }

    public MoveType moveType() {
        return moveType;
    }

    public PieceType promotedTo() {
        return promotedTo;
    }

    public void setPromotedTo(PieceType promotedTo) {
        this.promotedTo = promotedTo;
    }
}
