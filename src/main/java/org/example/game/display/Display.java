package org.example.game.display;

import org.example.game.ChessGame;
import org.example.game.entities.Piece;
import org.example.game.entities.Position;
import org.example.game.entities.Team;

import java.util.List;

public class Display {

    private final ChessGame game;

    public Display(ChessGame game) {
        this.game = game;
    }

    public static final class Ansi {
        public static final String RESET = "\u001B[0m";
        public static final String WHITE = "\u001B[97m"; // blanc brillant
        public static final String BLACK = "\u001B[30m"; // noir
        public static final String ORANGE = "\u001B[38;5;208m";
        public static final String RED_BOLD = "\u001B[1;31m";
        public static final String BLUE = "\u001B[34m";
    }

    public String printPossibleMovesFor(String pos) {
        Position p = Position.of(pos);
        List<Position> moves = game.getLegalMovesPos(p);

        StringBuilder chaine = new StringBuilder();

        chaine.append(">>>>> Possible moves for ").append(game.getMapBoard().get(p).getType().getAlgebricNotation()).append(p.toAlgebraic()).append(" :\n");

        for (int i = 7; i >= 0; i--) {

            chaine.append(Display.Ansi.BLUE)
                    .append(i + 1)
                    .append(" ")
                    .append(Display.Ansi.RESET);

            for (int j = 0; j < 8; j++) {

                Position current = new Position(j, i);
                Piece piece = game.getMapBoard().get(current);

                if (current.equals(p)) {
                    chaine.append(Display.Ansi.RED_BOLD);
                } else if (moves.contains(current)) {
                    chaine.append(Display.Ansi.ORANGE);
                } else if (piece != null) {
                    chaine.append(piece.getTeam() == Team.WHITE
                            ? Display.Ansi.WHITE
                            : Display.Ansi.BLACK);
                }

                chaine.append(piece == null ? "·" : piece.getType().getAlgebricNotation())
                        .append(Display.Ansi.RESET)
                        .append("  ");
            }

            chaine.append("\n");
        }
        chaine.append("  ");

        for (char c = 'a'; c <= 'h'; c++) {
            chaine.append(Display.Ansi.BLUE)
                    .append(c)
                    .append(Display.Ansi.RESET)
                    .append("  ");
        }

        return chaine.toString();
    }

    @Override
    public String toString() {
        StringBuilder chaine = new StringBuilder();

        for (int i = 7; i >= 0; i--) {

            chaine.append(Display.Ansi.BLUE)
                    .append(i + 1)
                    .append(" ")
                    .append(Display.Ansi.RESET);

            for (int j = 0; j < 8; j++) {

                Piece piece = game.getMapBoard().get(new Position(j, i));

                if (piece == null) {
                    chaine.append("·");
                } else {
                    String color = piece.getTeam() == Team.WHITE
                            ? Display.Ansi.WHITE
                            : Display.Ansi.BLACK;

                    chaine.append(color)
                            .append(piece.getType().getAlgebricNotation())
                            .append(Display.Ansi.RESET);
                }

                chaine.append("  ");
            }

            chaine.append("\n");
        }

        chaine.append("  ");

        for (char c = 'a'; c <= 'h'; c++) {
            chaine.append(Display.Ansi.BLUE)
                    .append(c)
                    .append(Display.Ansi.RESET)
                    .append("  ");
        }

        return chaine.toString();
    }

}
