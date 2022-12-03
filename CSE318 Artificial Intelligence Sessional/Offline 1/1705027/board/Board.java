package com.company.board;

import com.company.Solvability;

import javax.swing.*;

import com.company.Utils;
import com.company.Utils.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

import static com.company.Utils.*;

public class Board {
    private int [][] board;
    private int k,i,j, astericPos;
    private int gncost;
    private int fncost;
    private int heuristicCost;
    private int heuristicChoice; //1:hamming 2: manhattan 3:linear conflicts
    private int astericI;
    private int astericJ;
    public Board previousNode=null;


    public Board(int[][] board, int k) {
        this.k = k;
        this.board = new int[k][k];
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++)
                this.board[i][j] = board[i][j];
        }
    }

    public Board(int k, int[][] board, int astericPos) {
        this.board = new int[k][k];
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++)
                this.board[i][j] = board[i][j];
        }
        this.k = k;
        this.astericPos = astericPos;
    }

    public Board(int k, int[][] board, int gncost, int heuristicChoice) {
        this.board = new int[k][k];
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++)
                this.board[i][j] = board[i][j];
        }
        this.k = k;
        this.gncost = gncost;
        this.heuristicChoice=heuristicChoice;
    }

    public int[][] getBoard() {
        return board;
    }

    public int getK() {
        return k;
    }

    public void setGncost(int gncost) {
        this.gncost = gncost;
    }

    public int getGncost() {
        return gncost;
    }

    public void setHeuristicChoice(int heuristicChoice) {
        this.heuristicChoice = heuristicChoice;
    }

    public int getFncost() {
        if(heuristicChoice==1){
            heuristicCost=hamming_distance(k, board);
            fncost = gncost+heuristicCost;
        }
        else if(heuristicChoice==2){
            heuristicCost=manhattan_distance(k,board);
            fncost = gncost+heuristicCost;
        }
        else{
            heuristicCost=linear_conflicts(k,board);
            fncost = gncost+heuristicCost;
        }
        //System.out.println(heuristicChoice+" : "+ heuristicCost);
        return fncost;
    }

    public int getHeuristicCost(){
        return heuristicCost;
    }

    public int getVal(int row, int column){
        if(row>=k || column>=k){
            System.out.println("out of bound row or column index");
            return 0;
        }
        return board[row][column];
    }

    public boolean test_solvable(){
        Solvability solvability = new Solvability(k, board, astericPos);
        boolean solvable = solvability.test_solvable();
        if(solvable) System.out.println("The board is solvable.");
        else System.out.println("the board is not solvable");
        return solvable;
    }

    public void setAstericIJ(){
        for(i=0;i<k;i++){
            for(j=0;j<k;j++){
                if(board[i][j] == k*k){
                    astericI=i;
                    astericJ=j;
                    break;
                }
            }
        }
    }

    //direction
    //1:up 2:down 3:right 4:left
    public Board getChild(int direction){
        int new_i, new_j;
        setAstericIJ();
        if(direction==1){ //up
            new_i = astericI-1;
            new_j = astericJ;
        }
        else if(direction==2){ //down
            new_i = astericI +1;
            new_j = astericJ;
        }
        else if(direction==3){ //right
            new_i = astericI;
            new_j = astericJ+1;
        }
        else{//left
            new_i = astericI;
            new_j = astericJ-1;
        }

        if(new_i<0 || new_i>=k || new_j<0 || new_j>=k) return null;

        int [][] newChild = new int[k][k];

        for(i=0;i<k;i++){
            for(j=0;j<k;j++){
                newChild[i][j]= board[i][j];
            }
        }

        newChild[new_i][new_j] = board[astericI][astericJ];
        newChild[astericI][astericJ]= board[new_i][new_j];

        Board b = new Board(k, newChild, gncost+1, heuristicChoice);
        return b;

    }

    @Override
    public boolean equals(Object o){

        if(o == null) return false;
        if(o == this ) return true;
        if( !(o instanceof Board)) return false;

        Board b = (Board) o;

        for(i=0;i<k;i++){
            for(j=0;j<k;j++){
                if(board[i][j] != b.getVal(i,j)){
                    //System.out.println(board[i][j] +" "+b.getVal(i,j));
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board);
    }

    public void printBoard(){
        System.out.println();
        print2D(k, board);
        System.out.println();
    }

    public String getStringseq(){
        String s="";
        for(i=0;i<k;i++){
            for(j=0;j<k;j++)
                s+= String.valueOf(board[i][j]);
        }

        return s;
    }
}

