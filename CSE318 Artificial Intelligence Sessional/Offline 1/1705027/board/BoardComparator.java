package com.company.board;

import java.util.Comparator;

public class BoardComparator implements Comparator<Board> {
    public int compare(Board b1, Board b2){
        int b1cost = b1.getFncost();
        int b2cost = b2.getFncost();
        if (b1cost> b2cost)
            return 1;
        else if(b1cost< b2cost)
            return -1;
        else {
            int b1gncost = b1.getGncost();
            int b2gncost = b2.getGncost();
            if(b1gncost < b2gncost) return 1;
            else if(b1gncost > b2gncost) return -1;
            return 0;
        }
    }
}
