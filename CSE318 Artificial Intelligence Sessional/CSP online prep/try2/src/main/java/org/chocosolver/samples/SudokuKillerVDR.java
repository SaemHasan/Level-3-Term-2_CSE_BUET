package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;

public class SudokuKillerVDR {
    void modelAndSolve() {
        Model model = new Model("SudokuWithMatrix");

        IntVar[][] board = model.intVarMatrix("B", 9, 9, 1, 9);

        // ------------------ 0
        model.sum(
                new IntVar[]{board[0][0],
                        board[1][0]
                }, "=", 13
        ).post();

        model.sum(
                new IntVar[]{
                        board[0][1], board[0][2], board[0][3], board[0][4],
                        board[1][4]
                }, "=", 21
        ).post();

        model.sum(
                new IntVar[]{
                        board[0][5],
                        board[1][5]
                }, "=", 12
        ).post();

        model.sum(
                new IntVar[]{
                        board[0][6],
                        board[1][6], board[1][7],
                        board[2][7]
                }, "=", 24
        ).post();

        model.sum(
                new IntVar[]{
                        board[0][7], board[0][8],
                        board[1][8],
                        board[2][8]
                }, "=", 17
        ).post();

        // ------------------ 1
        model.sum(
                new IntVar[]{
                        board[1][1],
                        board[2][1]
                }, "=", 9
        ).post();

        model.sum(
                new IntVar[]{
                        board[1][2],
                        board[2][2]
                }, "=", 11
        ).post();

        model.sum(
                new IntVar[]{
                        board[1][3],
                        board[2][3],
                        board[3][3]
                }, "=", 13
        ).post();
        // ----------------- 2

        model.sum(
                new IntVar[]{
                        board[2][0],
                        board[3][0]
                }, "=", 5
        ).post();;

        model.sum(
                new IntVar[]{
                        board[2][4],
                        board[3][4]
                }, "=", 11
        ).post();


        model.sum(
                new IntVar[]{
                        board[2][5],
                        board[2][6]
                }, "=", 11
        ).post();

        for (int i = 0; i < 9; i++) {
            model.allDifferent(board[i]).post();
        }

        for (int i = 0; i < 9; i++) {
            IntVar[] temp = new IntVar[]{};
            for (int j = 0; j < 9; j++) {
                temp = ArrayUtils.concat(temp, board[j][i]);
            }
//            System.out.println(temp.length);
            model.allDifferent(temp).post();
        }

        for (int rowStart = 0; rowStart < 9; rowStart += 3) {
            for (int columnStart = 0; columnStart < 9; columnStart += 3) {
                IntVar[] temp = new IntVar[]{};
                for (int i = 0; i < 3; i++) {
                    for (int j = 0; j < 3; j++) {
                        temp = ArrayUtils.concat(temp, board[rowStart + i][columnStart + j]);
                    }
                }
                model.allDifferent(temp).post();
            }
        }

        Solution solution = model.getSolver().findSolution();
//        System.out.println(solution);
//        model.getSolver().showSolutions();
        if (solution != null) {
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    IntVar k = board[i][j];

                    System.out.print(k.getValue() + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("NOOOOOOOOOOO");
        }
    }

    public static void main(String[] args) {
        new SudokuKillerVDR().modelAndSolve();
    }
}
