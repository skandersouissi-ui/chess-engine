package org.example.game.entities;

import java.util.Objects;

public class Piece {

    private PieceType type;
    private Team team;
    private Position position;
    private boolean hasMoved=false;

    public Piece(PieceType pieceType,Team team,Position position){
        this.type=pieceType;
        this.team=team;
        this.position=position;
    }

    public PieceType getType() {
        return type;
    }

    public Team getTeam() {
        return team;
    }

    public Position getPosition() {
        return position;
    }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setMovedStatue(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return team + " " + type;
    }

    public String algebricToString(){
        return team.toAlgebraic()+type.getAlgebricNotation();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Piece other)) return false;
        return type == other.type && team == other.team && position.equals(other.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, team, position);
    }

}
