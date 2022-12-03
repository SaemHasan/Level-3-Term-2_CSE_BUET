package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

public class Heuristic4 implements Heuristic{
    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        int diffStorage = mancalaBoard.getFirstPlayerStonesinStorage() - mancalaBoard.getSecondPlayerStonesInStorage();
        int w1 = 5;
        int w3 = 4;
        int w4 = 3;

        return ( (w1*(diffStorage)) + (w3 * stats.getAddtionalMove()) + (w4*stats.getCapturedStones()) );
    }
}
