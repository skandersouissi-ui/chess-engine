package org.example.game.bots;

import org.example.game.ChessGame;
import org.example.game.entities.Move;
import org.example.game.entities.Piece;
import org.example.game.entities.Team;

public class BotTestA implements Bot{

    private final Team team;
    private final ChessGame game;
    private long seed;

    BotTestA(Team team, ChessGame game){
        this.team = team;
        this.game = game;
    }

    @Override
    public void play() {


    }

    @Override
    public long getSeed() {
        return 0;
    }

}
