package org.example.game;

import org.example.game.entities.*;
import org.example.game.exceptions.GameOverException;
import org.example.game.exceptions.NeedPromotionException;
import org.example.game.gamestate.Board;
import org.example.game.gamestate.GameState;
import org.example.game.ruleset.MoveGenerator;
import org.example.game.ruleset.RuleValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ChessGame implements Game {

    private final GameState gameState;
    private final MoveGenerator moveGenerator;
    private final RuleValidator ruleValidator;

    public ChessGame(){
        gameState = new GameState();
        moveGenerator = new MoveGenerator(gameState);
        ruleValidator = new RuleValidator(gameState,moveGenerator);
    }

    public ChessGame(Board board){
        gameState = new GameState(board);
        moveGenerator = new MoveGenerator(gameState);
        ruleValidator = new RuleValidator(gameState,moveGenerator);
    }


    public Move move(String from,String to){
        return move(Position.of(from),Position.of(to));
    }

    public Move move(Position from, Position to)throws GameOverException, NeedPromotionException{

        if(isGameOver()) throw new GameOverException("Game is over");

        Move move = ruleValidator.findAskedMove(from,to);

        checkCurrentTurn(move.moved());

        if(isPromotion(move.moved(),move.to())) throw new NeedPromotionException("Promotion: you have to use ChessGame.movePromotion()");

        Move moveDone = gameState.move(move);

        gameState.refreshKingCheckStatut(moveGenerator);

        return moveDone;
    }

    public Move movePromotion(String from,String to,PieceType promotedTo){
        return movePromotion(Position.of(from),Position.of(to),promotedTo);
    }

    public Move movePromotion(Position from, Position to, PieceType promotedTo){

        if(isGameOver()) throw new IllegalStateException("Game is over");

        if(promotedTo != PieceType.QUEEN &&
                promotedTo != PieceType.BISHOP &&
                promotedTo != PieceType.KNIGHT &&
                promotedTo != PieceType.ROOK){
            throw new IllegalStateException("Promotion: illegal piece type " + promotedTo);
        }

        Move m = ruleValidator.findAskedMove(from,to);

        checkCurrentTurn(m.moved());
        if(!isPromotion(m.moved(),m.to())){throw new IllegalStateException("Promotion: illegal move, your move is not a promotion" + m.to());}

        Move promotion = m.withPromotion(promotedTo);
        Move moveDone = gameState.move(promotion);

        gameState.refreshKingCheckStatut(moveGenerator);

        return moveDone;
    }

    public Move undo(){
        if(gameState.isMoveStackEmpty()){
            throw new IllegalStateException("No moves played in board");
        }
        Move undoDone = gameState.undo();
        gameState.refreshKingCheckStatut(moveGenerator);
        return undoDone;
    }

    public List<Move> getLegalMoves(Position pos){
        return ruleValidator.getLegalMoves(pos);
    }

    public List<Move> getAllLegalMoves(Team team){
        List<Position> pieces = getPieces(team).stream().map(Piece::getPosition).toList();
        List<Move> allMoves = new ArrayList<>();
        pieces.forEach(pos -> allMoves.addAll(getLegalMoves(pos)));
        return allMoves;
    }

    public boolean isGameOver() {
        return isCheckMate(Team.BLACK)
                || isCheckMate(Team.WHITE)
                || isDraw();
    }

    public boolean isDraw(){
        return ruleValidator.isStaleMate(Team.BLACK)
                || ruleValidator.isStaleMate(Team.WHITE)
                || ruleValidator.isDraw50Moves()
                || ruleValidator.hasNotEnoughPieces()
                || gameState.getZobristHash().isThreefoldRepetition();
    }

    public List<Position> getLegalMovesPos(Position pos){
        return getLegalMoves(pos).stream().map(Move::to).toList();
    }


    public Team winner() {
        boolean whiteMate = isCheckMate(Team.WHITE);
        boolean blackMate = isCheckMate(Team.BLACK);
        boolean isDraw = isDraw();

        if (!(whiteMate || blackMate || isDraw)) {
            throw new IllegalStateException("Game not over");
        }

        if (isDraw) {
            return Team.DRAW;
        }

        return whiteMate ? Team.BLACK : Team.WHITE;
    }

    private boolean isPromotion(Piece piece, Position to) {
        if (piece.getType() != PieceType.PAWN) return false;

        return (piece.getTeam() == Team.WHITE && to.row() == 7)
                || (piece.getTeam() == Team.BLACK && to.row() == 0);
    }


    public Team currentTurn(){
        return gameState.getCurrentTeam();
    }

    @Override
    public boolean isKingInCheck(Team team){
        return gameState.isKingInCheck(team);
    }

    @Override
    public boolean isCheckMate(Team team) {
        return isKingInCheck(team) && !ruleValidator.hasLegalMoves(team);
    }

    public boolean isStaleMate(Team team){
        return ruleValidator.isStaleMate(team);
    }


    public Map<Position,Piece> getMapBoard(){
        return gameState.getBoard().getBoard();
    }

    public Piece getPiece(Position pos){
        return gameState.getBoard().getPiece(pos);
    }

    public void checkCurrentTurn(Piece piece){
        if(gameState.getCurrentTeam()!=piece.getTeam()){
            throw new IllegalStateException( "Current turn: "+ gameState.getCurrentTeam() + " team ");
        }
        gameState.changeCurrentTeam();
    }

    public GameState getGameState() {
        return gameState;
    }

    public List<Piece> getPieces(Team team){
        return gameState.getBoard().getPieces(team);
    }

    public long getCurrentHash(){
        return gameState.getZobristHash().getCurrentHash();
    }

}
