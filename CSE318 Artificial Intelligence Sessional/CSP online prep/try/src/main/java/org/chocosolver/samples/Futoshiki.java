package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class Futoshiki {
    public static void main(String[] args) {
        int i, j, row, col, k;

        Model model = new Model("Futoshiki puzzle");

        IntVar[][] bd = model.intVarMatrix("bd", 9, 9, 1, 9);

        for (i = 0; i < 9; i++) {
            model.allDifferent(bd[i]).post();
        }

        for (i = 0; i < 9; i++) {
            IntVar[] temp = new IntVar[]{};
            for (j = 0; j < 9; j++) {
                temp = ArrayUtils.concat(temp, bd[j][i]);
            }

            model.allDifferent(temp).post();
        }

        //model.arithm(bd[0][1], "=", 5);

        model.arithm(bd[0][1],"=", 5).post();
        model.arithm(bd[0][2],"=", 4).post();
        model.arithm(bd[0][6],"=", 8).post();
        model.arithm(bd[2][4],"=", 6).post();
        model.arithm(bd[4][0],"=", 2).post();
        model.arithm(bd[4][2],"=", 8).post();
        model.arithm(bd[5][1],"=", 7).post();
        model.arithm(bd[5][2],"=", 6).post();
        model.arithm(bd[5][3],"=", 2).post();
        model.arithm(bd[5][7],"=", 9).post();
        model.arithm(bd[6][2],"=", 3).post();
        model.arithm(bd[6][8],"=", 5).post();
        model.arithm(bd[7][3],"=", 6).post();
        model.arithm(bd[7][6],"=", 5).post();
        model.arithm(bd[8][1],"=", 3).post();
        model.arithm(bd[8][5],"=", 1).post();


        model.arithm(bd[0][0],">", bd[1][0]).post();
        model.arithm(bd[2][0],">", bd[3][0]).post();
        model.arithm(bd[4][0],">", bd[5][0]).post();
        model.arithm(bd[1][1],">", bd[0][1]).post();
        model.arithm(bd[3][1],">", bd[3][2]).post();
        model.arithm(bd[5][1],">", bd[4][1]).post();
        model.arithm(bd[2][2],">", bd[1][2]).post();
        model.arithm(bd[3][2],">", bd[2][2]).post();
        model.arithm(bd[2][3],">", bd[2][2]).post();
        model.arithm(bd[7][2],">", bd[7][3]).post();
        model.arithm(bd[0][4],">", bd[0][3]).post();
        model.arithm(bd[1][4],">", bd[2][4]).post();
        model.arithm(bd[1][5],">", bd[1][4]).post();
        model.arithm(bd[3][4],">", bd[4][4]).post();
        model.arithm(bd[7][4],">", bd[7][5]).post();
        model.arithm(bd[2][6],">", bd[3][6]).post();
        model.arithm(bd[3][6],">", bd[3][7]).post();
        model.arithm(bd[4][6],">", bd[4][5]).post();
        model.arithm(bd[8][6],">", bd[8][7]).post();
        model.arithm(bd[0][7],">", bd[1][7]).post();
        model.arithm(bd[1][7],">", bd[2][7]).post();
        model.arithm(bd[1][8],">", bd[1][7]).post();
        model.arithm(bd[2][8],">", bd[2][7]).post();
        model.arithm(bd[3][8],">", bd[3][7]).post();
        model.arithm(bd[6][8],">", bd[5][8]).post();
        model.arithm(bd[7][8],">", bd[6][8]).post();


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
