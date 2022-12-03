package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

public class Heuristic1 implements Heuristic{

    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        return mancalaBoard.getFirstPlayerStonesinStorage() - mancalaBoard.getSecondPlayerStonesInStorage();
    }
}
