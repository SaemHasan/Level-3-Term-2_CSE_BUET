package com.company.search;

import com.company.board.MancalaBoard;

public class Node {
    private MancalaBoard parent;
    private int heuristicval;

    public Node(MancalaBoard parent, int heuristicval) {
        this.parent = parent;
        this.heuristicval = heuristicval;
    }

    public int getHeuristicval() {
        return heuristicval;
    }

    public MancalaBoard getParent() {
        return parent;
    }
}
