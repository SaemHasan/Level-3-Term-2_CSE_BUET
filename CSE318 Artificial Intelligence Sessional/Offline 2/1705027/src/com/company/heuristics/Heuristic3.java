package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

import java.util.Random;

public class Heuristic3 implements Heuristic{

    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        int diffStorage = mancalaBoard.getFirstPlayerStonesinStorage() - mancalaBoard.getSecondPlayerStonesInStorage();
        int myside = mancalaBoard.getStones_on_my_side();
        int opponentSide = mancalaBoard.getStones_on_opponentSide();
        int w1 = 10;
        int w2 = 0;
        int w3 = 8;

        return ((w1*(diffStorage)) + (w2 * (myside - opponentSide)) + w3 * stats.getAddtionalMove());
    }
}
