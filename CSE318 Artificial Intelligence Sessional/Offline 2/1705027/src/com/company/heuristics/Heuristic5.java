package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

public class Heuristic5 implements Heuristic{
    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        int diffStorage = mancalaBoard.getFirstPlayerStonesinStorage() - mancalaBoard.getSecondPlayerStonesInStorage();
        int w1 = 4;
        int w4 = 3;
        int w5 = 2;

        return ( (w1*(diffStorage)) + (w4*stats.getCapturedStones()) + (w5*stats.getBinclosed_to_storage()) );
    }
}
