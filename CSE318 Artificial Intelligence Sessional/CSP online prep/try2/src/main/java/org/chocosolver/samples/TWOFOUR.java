/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 */
package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;

/**
 * Verbal arithmetic
 * <p>
 * @author Charles Prud'homme
 * @since 27/05/2016.
 */
public class TWOFOUR {

    public void modelAndSolve(){
        Model model = new Model("TWO+TWO=FOUR");

        IntVar T = model.intVar("T", 1, 9, false);
        IntVar W = model.intVar("W", 0, 9, false);
        IntVar O = model.intVar("O", 0, 9, false);
        IntVar F = model.intVar("F", 1, 9, false);
        IntVar U = model.intVar("U", 0, 9, false);
        IntVar R = model.intVar("R", 0, 9, false);

        model.allDifferent(new IntVar[]{T,W,O,F,U,R}).post();

        IntVar[] a = new IntVar[]{
                T,W,O,
                T,W,O,
                F,O,U,R
        };

        int [] c= new int[]{
                100,10,1,
                100,10,1,
                -1000,-100,-10,-1
        };

        model.scalar(a,c,"=",0).post();

        Solver solver = model.getSolver();
        solver.showStatistics();
        solver.showSolutions();
        solver.findSolution();
    }

    public static void main(String[] args) {
        new TWOFOUR().modelAndSolve();
    }
}