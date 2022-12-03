package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

public class Heuristic6 implements Heuristic{
    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        int diffStorage = mancalaBoard.getFirstPlayerStonesinStorage() - mancalaBoard.getSecondPlayerStonesInStorage();
        int w1 = 4;
        int w2 = 3;
        return (w1*diffStorage) + (w2*stats.getStonesClosed_to_storage());
    }
}
