package com.company;

public class Solvability {
    private int [][] board;
    private int k,i,j, astericPos;

    public Solvability(int k, int[][] board, int astericPos) {
        this.board = board;
        this.k = k;
        this.astericPos = astericPos;
    }

    public boolean test_solvable(){
        int inversions = inversion_count();
        //System.out.println("Total inversions : "+inversions);
        if(k%2==1 && inversions%2==0){
            return true;
        }
        else if(k%2==0){
            if(astericPos%2==0 && inversions%2==1){
                return true;
            }
            else if(astericPos%2==1 && inversions%2==0){
                return true;
            }
        }

        return false;
    }


    private int inversion_count(){
        int count =0;

        int [] board1D = Utils.conv2dTo1D(k,board);

        int size = k*k;
        for(i=0;i<size;i++){
            for(j=i+1;j<size;j++){
                if(board1D[i] != size && board1D[i]> board1D[j]){
                    count++;
                }
            }
        }
        return count;
    }
}
