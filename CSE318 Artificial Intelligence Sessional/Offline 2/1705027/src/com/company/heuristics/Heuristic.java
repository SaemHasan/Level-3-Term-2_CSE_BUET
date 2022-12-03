package com.company.heuristics;

import com.company.board.MancalaBoard;
import com.company.board.Stats;
import com.company.search.DLS;


public interface Heuristic {
    int getHeuristicval(MancalaBoard mancalaBoard, Stats stats);

    default int selectbin(MancalaBoard mancalaBoard){
        DLS dls = new DLS();
        return dls.minimax(mancalaBoard, mancalaBoard.getActivePlayerDepth());
    }
}
