package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class SudokuKiller {
    
    public static void main(String[] args){
        int i,j,row,col,k;

        Model model = new Model("sudoku killer");

        IntVar [][]bd = model.intVarMatrix("bd", 9,9,1,9);

        for(i=0;i<9;i++){
            model.allDifferent(bd[i]).post();
        }

        for(i=0;i<9;i++){
            IntVar []temp = new IntVar[]{};
            for(j=0;j<9;j++){
                temp = ArrayUtils.concat(temp, bd[j][i]);
            }

            model.allDifferent(temp).post();
        }

        for(row=0;row<9;row+=3){
            for(col=0;col<9;col+=3){
                IntVar []temp = new IntVar[]{};
                for(i=0;i<3;i++){
                    for(j=0;j<3;j++){
                        temp = ArrayUtils.concat(temp, bd[i+row][j+col]);
                    }
                }
                model.allDifferent(temp).post();
            }
        }

        model.sum(new IntVar[]{
                bd[0][0], bd[1][0]
        }, "=", 13).post();

        model.sum(new IntVar[]{
                bd[2][0], bd[3][0]
        }, "=", 5).post();

        model.sum(new IntVar[]{
                bd[4][0], bd[4][1]
        }, "=", 8).post();

        model.sum(new IntVar[]{
                bd[5][0], bd[6][0]
        }, "=", 17).post();

        model.sum(new IntVar[]{
                bd[7][0], bd[8][0]
        }, "=", 5).post();

        model.sum(new IntVar[]{
                bd[0][1], bd[0][2], bd[0][3], bd[0][4], bd[1][4]
        }, "=", 21).post();

        model.sum(new IntVar[]{
                bd[1][1], bd[2][1]
        }, "=", 9).post();

        model.sum(new IntVar[]{
                bd[3][1], bd[3][2], bd[4][2], bd[4][3], bd[5][1], bd[5][2]
        }, "=", 33).post();

        model.sum(new IntVar[]{
                bd[6][1], bd[7][1]
        }, "=", 13).post();

        model.sum(new IntVar[]{
                bd[1][2], bd[2][2]
        }, "=", 11).post();

        model.sum(new IntVar[]{
                bd[6][2], bd[7][2]
        }, "=", 5).post();

        model.sum(new IntVar[]{
                bd[1][3], bd[2][3], bd[3][3]
        }, "=", 13).post();

        model.sum(new IntVar[]{
                bd[5][3], bd[6][3], bd[7][3]
        }, "=", 13).post();

        model.sum(new IntVar[]{
                bd[2][4], bd[3][4]
        }, "=", 11).post();

        model.sum(new IntVar[]{
                bd[4][4], bd[4][5], bd[4][6], bd[4][7], bd[4][8]
        }, "=", 20).post();


        model.sum(new IntVar[]{
                bd[5][4], bd[6][4]
        }, "=", 10).post();

        model.sum(new IntVar[]{
                bd[7][4], bd[8][4], bd[8][3], bd[8][2], bd[8][1]
        }, "=", 31).post();

        model.sum(new IntVar[]{
                bd[0][5], bd[1][5]
        }, "=", 12).post();

        model.sum(new IntVar[]{
                bd[2][5], bd[2][6]
        },"=", 11).post();

        model.sum(new IntVar[]{
                bd[3][5], bd[3][6]
        }, "=", 15).post();

        model.sum(new IntVar[]{
                bd[5][5], bd[5][6]
        }, "=", 9).post();

        model.sum(new IntVar[]{
                bd[6][5], bd[6][6]
        }, "=", 10).post();


        model.sum(new IntVar[]{
                bd[7][5], bd[8][5]
        }, "=", 9).post();


        model.sum(new IntVar[]{
                bd[0][6], bd[1][6],bd[1][7], bd[2][7]
        }, "=", 24).post();


        model.sum(new IntVar[]{
                bd[0][7], bd[0][8], bd[1][8], bd[2][8]
        }, "=", 17).post();

        model.sum(new IntVar[]{
                bd[3][7], bd[3][8]
        }, "=", 9).post();

        model.sum(new IntVar[]{
                bd[5][7], bd[5][8]
        }, "=", 8).post();


        model.sum(new IntVar[]{
                bd[6][7], bd[7][7], bd[7][6], bd[8][6]
        }, "=", 18).post();


        model.sum(new IntVar[]{
                bd[6][8], bd[7][8], bd[8][8], bd[8][7]
        }, "=", 25).post();


        Solver solver = model.getSolver();
        solver.showStatistics();
        solver.showSolutions();
        solver.findSolution();

        for ( i = 0; i < 9; i++)
        {
            for ( j = 0; j < 9; j++)
            {
                System.out.print(" ");/* get the value for the board position [i][j] for the solved board */
                k = bd [i][j].getValue();
                System.out.print(k );
            }
            System.out.println();
        }
    }


}
