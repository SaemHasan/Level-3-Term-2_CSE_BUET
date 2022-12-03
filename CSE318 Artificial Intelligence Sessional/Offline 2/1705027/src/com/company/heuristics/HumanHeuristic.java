package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;
import com.company.play.Utils;

public class HumanHeuristic implements Heuristic{

    @Override
    public int getHeuristicval(MancalaBoard mancalaBoard, Stats stats) {
        return 0;
    }

    @Override
    public int selectbin(MancalaBoard mancalaBoard) {
        return Utils.getUserBinSelectInput(mancalaBoard, mancalaBoard.getActivePlayer());
    }
}
