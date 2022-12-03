package com.company;

import com.company.board.Board;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {

    public static int [][] finalBoard;
    public static Board goal;

    public static void main(String[] args){
	// write your code here
        int k, val, astericPos =0;
        String inputVal;
        int [][] initialBoard;

        //Scanner input = new Scanner(System.in);
        File file = new File("src/input.txt");
        Scanner inputFromFile= null;
        try {
            inputFromFile = new Scanner(file);
        }catch (FileNotFoundException e){
            e.printStackTrace();
            System.exit(0);
        }



        k = Integer.parseInt(inputFromFile.nextLine());

        initialBoard = new int[k][k];
        finalBoard = new int[k][k];

        for(int i=0;i<k;i++){
            inputVal = inputFromFile.nextLine();
            String []values = inputVal.split(" ");
            for(int j=0;j<k;j++){
               inputVal = values[j];

               if(inputVal.contains("*") || inputVal.equalsIgnoreCase("0")){
                   val = k*k;
                   astericPos = k-i;
               }
               else{
                   val= Integer.parseInt(inputVal);
               }

               initialBoard[i][j] = val;
               finalBoard[i][j] = i * k + j+1;
            }
        }

        goal = new Board(finalBoard, k);

        //goal.printBoard();

        Board board = new Board(k, initialBoard, astericPos);
        boolean solvable = board.test_solvable();


        if(solvable) {
            //1:hamming 2: manhattan 3:linear conflicts
            AstarSearch search = new AstarSearch(board, goal, 2);
            search.aStarSearch();
            search.printDetails();
        }
        else{

        }
    }
}
