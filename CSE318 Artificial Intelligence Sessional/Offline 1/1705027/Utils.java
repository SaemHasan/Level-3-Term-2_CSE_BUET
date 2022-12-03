package com.company;

public class Utils {

    public static int diffWithFinalBoard(int k, int[][] board){
        int cnt=0;
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++){
                if(board[i][j]!= k*k && board[i][j] != Main.finalBoard[i][j]) cnt++;
            }
        }
        return cnt;
    }

    public static int hamming_distance(int k, int[][] board){
        int distance = diffWithFinalBoard(k, board);

        //System.out.println("Hamming distance : "+ distance);
        return distance;
    }

    public static int manhattan_distance(int k, int [][] board){
        int distance =0,i,j, row, column,d,val;
        for(i=0;i<k;i++){
            for(j=0;j<k;j++){
                val = board[i][j]-1;
                if(val+1!= k*k) {
                    row = val / k;
                    column = val % k;
                    d = Math.abs(row - i) + Math.abs(column - j);
                    //System.out.println(val + 1 + " : \t" + d);
                    distance += d;
                }
            }
        }

        //System.out.println("Manhattan distance : "+ distance);
        return distance;
    }

    public static int linear_conflicts(int k, int[][] board){
        int distance=0;
        distance = countConflicts(k, board);
        //System.out.println("conflicts : "+ distance);
        //if(distance>0) print2D(k, board);
        distance = (manhattan_distance(k, board)) +  (2 * distance);
        //if(distance==0) print2D(k, board);
        return distance;
    }

    private static boolean valueInCorrectRow(int k, int row, int val){
        for(int i=0;i<k;i++){
            if((row*k+i+1)==val) return true;
        }
        return false;
    }

    private static boolean valueInCorrectPlace(int i, int j, int val){
        return Main.finalBoard[i][j] == val;
    }

    private static int countConflicts(int k, int[][] board){
        int cnt =0, i,j,m;

        for(i=0;i<k;i++){
            for(j=0;j<k-1;j++){
                if(board[i][j]!=k*k) {
                    for (m = j + 1; m < k; m++) {
                        if (valueInCorrectRow(k, i, board[i][j]) && valueInCorrectRow(k, i, board[i][m])) {
                            //System.out.println("value in correct row");
                            if (!valueInCorrectPlace(i, j, board[i][j]) && !valueInCorrectPlace(i, m, board[i][m])) {
                                if(board[i][j]>board[i][m])
                                    cnt++;
                            }
                        }
                    }
                }
            }
        }

        return cnt;
    }

    public static int[] conv2dTo1D(int k, int [][] board){
        int oneD[] = new int[k*k];
        int i,j;

        for (i=0; i<k; i++)
        {
            for (j=0; j<k; j++)
                oneD[i*k+j] = board[i][j];
        }

        return oneD;
    }

    public static void print2D(int k,int [][] arr){
        for(int i=0;i<k;i++){
            for(int j=0;j<k;j++){
                int v = arr[i][j];
                if(v!=k*k) System.out.print(v+"\t");
                else System.out.print("*\t");
            }
            System.out.println();
        }

    }
}
