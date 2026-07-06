package org.example;

import org.example.game.ChessGame;
import org.example.game.bots.*;
import org.example.game.display.Display;
import org.example.game.entities.Team;
import org.example.game.exceptions.GameOverException;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        playOneGame();

    }

    public static void print(Object o){
        System.out.println(o);
    }

    public static void doGames(){
        for(int i=0;i<100;i++){
            ChessGame game = new ChessGame();
            Bot white = new BotAlphaBeta(game,Team.WHITE,5);
            Bot black = new BotAlphaBeta(game,Team.BLACK,3);

            int cmpt = 0;

            while(true){
                try{
                    white.play();
                    black.play();
                    cmpt++;
                }catch(GameOverException e ){
                    System.out.println(game.winner() + " " + cmpt + " seedA/B : "+white.getSeed()+ " "+black.getSeed());
                    break;
                }
            }
        }
    }

    public static void playOneGame(){
        ChessGame game = new ChessGame();
        Display display = new Display(game);
        Bot white = new BotAlphaBeta(game,Team.WHITE,5);
        Bot black = new BotAlphaBeta(game,Team.BLACK,3);

        while(true){
            try{
                white.play();
                print(display);
                black.play();
                print(display);
            }catch(GameOverException e ){
                System.out.println(game.winner() + " " + " seedA/B : "+white.getSeed()+ " "+black.getSeed());
                break;
            }
        }
    }
}