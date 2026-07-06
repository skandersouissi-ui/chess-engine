package org.example.game.entities;

public enum PieceType {

    KING("K",1000),
    QUEEN("Q",9),
    BISHOP("B",3),
    ROOK("R",5),
    KNIGHT("N",3),
    PAWN("p",1);

    private final String algebric;
    private final int val;

    PieceType(String alg,int val){

        this.algebric=alg;
        this.val=val;
    }

    public String getAlgebricNotation() {
        return algebric;
    }

    public int getVal() {
        return val;
    }





}
