package com.company.play;

import com.company.board.MancalaBoard;

import javax.security.sasl.SaslServer;
import java.util.Scanner;

public class Utils {

    public static int getUserBinSelectInput(MancalaBoard mancalaBoard,int player){
        Scanner scanner = new Scanner(System.in);
        int selectedBin;
        while (true){
            System.out.println("Enter a valid bin number : (1-6)");
            selectedBin = scanner.nextInt();
            if(player==2) selectedBin = (mancalaBoard.getNumOfBins() + 1) - selectedBin;
            if(mancalaBoard.validBin(player, selectedBin)) return selectedBin;
            else{
                System.out.println("This bin is empty. Please select a valid bin.");
            }
        }
    }

    public static int selectHeuristic(int player){
        Scanner scanner = new Scanner(System.in);
        int selectHeuristic;
        while (true) {
            System.out.println("Heuristic 0 for Human");
            System.out.println("Enter heuristic number for player " + player + " (0-6):");
            selectHeuristic = scanner.nextInt();
            if(selectHeuristic<0 || selectHeuristic > 6) continue;
            break;
        }
        return selectHeuristic;
    }

    public static int selectFirstPlayer(){
        Scanner scanner = new Scanner(System.in);
        int player;
        while (true){
            System.out.println("Select which player will play first(player 1 or 2)");
            player = scanner.nextInt();
            if(player!=1 && player != 2){
                System.out.println("select a valid player");
                continue;
            }
            break;
        }
        return player;
    }

    public static int selectDepthForPlayer(int player){
        int d;
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Enter depth for player "+player);
            d = scanner.nextInt();
            if(d<0 || d>12){
                System.out.println("enter a valid depth(0-12)");
                continue;
            }
            break;
        }
        return d;
    }
}
