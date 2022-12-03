package com.company.play;

import com.company.Main;
import com.company.board.MancalaBoard;
import com.company.heuristics.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Random;

public class GamePlay {
    public static int DEPTH1 = 4;
    public static int DEPTH2 = 5;
    public final static int BONUS_TURN = 1, CAPTURE=2, MOVEDONE=0;
    public final static int player1 = 1, player2= 2;
    private MancalaBoard mancalaBoard;
    private Heuristic player1Heuristic;
    private Heuristic player2Heuristic;
    public static boolean PRINTALL = Main.PrintAll;

    public GamePlay() {
        mancalaBoard = new MancalaBoard();
        //if(PRINTALL) mancalaBoard.print_Board();
        DEPTH1 = 5;
        DEPTH2 = 6;
        player1Heuristic = new Heuristic1();
        player2Heuristic = new Heuristic1();
        mancalaBoard.setPlayer1Heuristic(player1Heuristic);
        mancalaBoard.setPlayer2Heuristic(player2Heuristic);
    }

    //for moves of a single player
    private void playerPlay(Heuristic heuristic, int player){
        int selectedBin;
        int result;
        while (true) {
            if(mancalaBoard.isGameOver()) break;
            mancalaBoard.setActivePlayer(player);
            if(PRINTALL) System.out.println("\nPlayer : "+player);
            selectedBin = heuristic.selectbin(mancalaBoard);
            result = mancalaBoard.make_a_move(player, selectedBin);
            if(result == CAPTURE && PRINTALL){
                System.out.println("\nNICE MOVE. CAPTURED STONES.\n");
            }

            if(PRINTALL) mancalaBoard.print_Board();

            if(result == MOVEDONE || result == CAPTURE) break; //break if no bonus turn
            else if(result == BONUS_TURN && PRINTALL){
                System.out.println("\nBonus turn for player "+player);
            }
        }
    }


    public int play(int firstplayer, int d1p1, int d2p2, Heuristic h1p1, Heuristic h2p2){
        mancalaBoard = new MancalaBoard();
        if(PRINTALL) mancalaBoard.print_Board();

        mancalaBoard.setFirstPlayer(firstplayer);

        mancalaBoard.setPlayer1Heuristic(h1p1);
        mancalaBoard.setPlayer1Depth(d1p1);

        mancalaBoard.setPlayer2Heuristic(h2p2);
        mancalaBoard.setPlayer2Depth(d2p2);


        while (true){
            if(firstplayer == player1) {
                playerPlay(h1p1, player1);
                playerPlay(h2p2, player2);
            }
            else{
                playerPlay(h2p2, player2);
                playerPlay(h1p1, player1);
            }
            if(mancalaBoard.isGameOver()) break;
        }
        return mancalaBoard.finalResult();
    }

    //taking all inputs from console to play the game
    public void takeInputFromConsoleToplay(){
        int h1p1 = Utils.selectHeuristic(player1);
        int h2p2 = Utils.selectHeuristic(player2);
        int firstPlayer = Utils.selectFirstPlayer();
        int d1p1 = 0;
        int d2p2 = 0;
        if(h1p1 != 0 ) d1p1 = Utils.selectDepthForPlayer(player1);
        if(h2p2 != 0 ) d2p2 = Utils.selectDepthForPlayer(player2);

        HeuristicFactory heuristicFactory = new HeuristicFactory();
        Heuristic heuristic1 = heuristicFactory.getHeuristicFromFactory(h1p1);
        Heuristic heuristic2 = heuristicFactory.getHeuristicFromFactory(h2p2);

        play(firstPlayer, d1p1, d2p2, heuristic1, heuristic2);
    }

    //for generating csv
    public void generateStat() throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter("History.csv");
        PrintWriter pw = new PrintWriter("Summary.csv");
        printWriter.println("Player 1 Heuristic,Player 2 Heuristic, First Player, Depth for player 1, Depth for player 2, Winner");
        pw.println("Player 1 Heuristic,Player 2 Heuristic, Player1 winning ratio, Player2 winning ratio, tie ratio");
        HeuristicFactory heuristicFactory = new HeuristicFactory();
        Random rand = new Random();
        for(int i=1;i<=6;i++){
            Heuristic h1p1 = heuristicFactory.getHeuristicFromFactory(i);
            for(int j=1;j<=6;j++){
                Heuristic h2p2 = heuristicFactory.getHeuristicFromFactory(j);
                int c1 = 0,c2 =0,t =0;
                int numofmatches=10;
                for(int k=1;k<=numofmatches;k++){
                    int d1p1 = rand.nextInt(10)+1;
                    int d2p2;
                    if((rand.nextInt(5)%2) == 1)
                        d2p2 = d1p1+rand.nextInt(3);
                    else d2p2 = Math.abs(d1p1 - rand.nextInt(3)) + 1;

                    int firstPlayer = rand.nextInt(2) % 2 + 1;

                    int r = play(firstPlayer, d1p1, d2p2, h1p1, h2p2);

                    printWriter.println(i+","+j+","+firstPlayer+","+d1p1+","+d2p2+","+r);
                    System.out.println("Done. i: "+i+", j: "+j+", k: "+k+", depth1 : "+d1p1+", depth2: "+d2p2+",  first player : "+firstPlayer+", winner : "+r);

                    if(r==0) t++;
                    else if(r==1) c1++;
                    else if(r==2) c2++;
                }
                float p1Win, p2Win, tieWin;
                p1Win = (float) ((c1*1.0)/numofmatches)*100;
                p2Win = (float) ((c2*1.0)/numofmatches)*100;
                tieWin = (float) ((t*1.0)/numofmatches)*100;

                pw.println(i+","+j+","+p1Win+","+p2Win+","+tieWin);
            }
        }

        pw.flush();
        pw.close();
        printWriter.flush();
        printWriter.close();
    }

    //if we want to play player 1 as first player. setting depth and heuristic in constructor of the class
    public void play(){
        if(PRINTALL) mancalaBoard.print_Board();
        mancalaBoard.setFirstPlayer(player1);
        mancalaBoard.setPlayer1Depth(DEPTH1);
        mancalaBoard.setPlayer2Depth(DEPTH2);
        while (true){
            playerPlay(player1Heuristic, player1);
            playerPlay(player2Heuristic, player2);
            if(mancalaBoard.isGameOver()) break;
        }
        mancalaBoard.finalResult();
    }

}
