package org.example.game.gamestate;

import org.example.game.entities.*;

import java.util.*;

public class Board {

    List<Piece> wPieces = new ArrayList<>();
    List<Piece> bPieces = new ArrayList<>();
    private final Map<Position, Piece> board;

    public Board() {
        board = new HashMap<>();
        initEmptyBoard();
        placePieces();

    }

    public Board(Map<Position, Piece> b) {
        board = new HashMap<>();
        initEmptyBoard();
        board.putAll(b);
    }


    public void initEmptyBoard() {
        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {
                board.put(new Position(col, row), null);
            }
        }
    }

    public void placePieces() {

        for (int col = 0; col < 8; col++) {
            for (int row = 0; row < 8; row++) {

                Position pos = new Position(col, row);
                Piece piece = createPiece(pos);

                if (piece != null) {
                    if(piece.getTeam()==Team.WHITE){
                        wPieces.add(piece);
                    }else{
                        bPieces.add(piece);
                    }
                    board.put(pos, piece);
                }
            }
        }
    }


    private Piece createPiece(Position pos) {

        int row = pos.row();
        char col = pos.colChar();

        Team team;

        if (row == 0 || row == 1) team = Team.WHITE;
        else if (row == 6 || row == 7) team = Team.BLACK;
        else return null;

        if (row == 1) return new Piece(PieceType.PAWN, team, pos);
        if (row == 6) return new Piece(PieceType.PAWN, team, pos);

        return switch (col) {

            case 'a', 'h' -> new Piece(PieceType.ROOK, team, pos);
            case 'b', 'g' -> new Piece(PieceType.KNIGHT, team, pos);
            case 'c', 'f' -> new Piece(PieceType.BISHOP, team, pos);
            case 'd' -> new Piece(
                     PieceType.QUEEN,
                    team,
                    pos
            );

            case 'e' -> new Piece(
                    PieceType.KING,
                    team,
                    pos
            );

            default -> null;
        };
    }


    public void applyMove(Position from, Position to) {
        Piece piece = board.get(from);
        Piece captured = board.get(to);

        if (captured != null) {
            getPieces(captured.getTeam()).remove(captured);
        }

        board.put(from, null);
        piece.setPosition(to);
        piece.setMovedStatue(true);
        board.put(to, piece);
    }

    public void applyUndo(Position from, Position to, boolean moved, Piece capturedPiece) {
        Piece piece = board.get(to);
        this.board.put(from, piece);
        piece.setPosition(from);
        piece.setMovedStatue(moved);

        this.board.put(to, capturedPiece);
        if (capturedPiece != null) {
            capturedPiece.setPosition(to);
            getPieces(capturedPiece.getTeam()).add(capturedPiece);
        }
    }

    public Piece getPiece(Position position) {
        return board.get(position);
    }

    public boolean hasPiece(Position position) {
        return board.get(position) != null;
    }

    public void deletePiece(Position position) {
        board.put(position, null);
    }

    public void addPiece(Position position, Piece piece) {
        board.put(position,piece);
    }

    public Map<Position, Piece> getBoard() {
        return Collections.unmodifiableMap(board);
    }



    public List<Piece> getPieces(Team team) {
        return Team.BLACK==team?bPieces:wPieces;
    }

}