package com.company;

import com.company.board.Board;
import com.company.board.BoardComparator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class AstarSearch {
    private Board board;
    private int moves;
    private Board finalGoal;
    private int heuristicChoice; //1:hamming 2: manhattan 3:linear conflicts
    private long exploredNodes;
    private long expandedNodes;
    private boolean printPath;

    public AstarSearch(Board board, Board finalGoal, int heuristicChoice) {
        this.board = board;
        this.finalGoal = finalGoal;
        this.heuristicChoice = heuristicChoice;
        moves=0;
        this.board.setHeuristicChoice(this.heuristicChoice);
        this.board.setGncost(0);
        expandedNodes=0;
        exploredNodes=0;
        printPath=true;
    }

    public void setPrintPath(boolean printPath) {
        this.printPath = printPath;
    }

    public void aStarSearch(){
        moves =0;
        expandedNodes=0;
        exploredNodes=0;
        PriorityQueue<Board> openList = new PriorityQueue<>(10, new BoardComparator());//open list
        HashMap<String, Board> closedList = new HashMap<>();

        board.previousNode =null;
        openList.add(board);

        while (! openList.isEmpty()){
            Board b = openList.poll();
            closedList.put(b.getStringseq(), b);
            expandedNodes++;
            //System.out.println("polled from open list");
            //b.printBoard();

            //System.out.println("in while loop : "+b.getHeuristicCost());
            if(b.equals(finalGoal)) {
                if(printPath){
                    System.out.println("\nOptimal Cost : "+b.getFncost());
                    System.out.println("\nOptimal Path : ");
                    printOptimalPath(b);
                    //printPath=false;
                }

                break;
            }

            moves++;

            for(int i=1;i<=4;i++){
                //direction
                //1:up 2:down 3:right 4:left
                Board child = b.getChild(i);
                if(child!=null){
                    //child.printBoard();
                    if(! closedList.containsKey(child.getStringseq())){
                        exploredNodes++;
                        child.previousNode = b;
                        openList.add(child);
                    }
                    else { }
                }

            }
        }
    }

    public void printOptimalPath(Board b){
        if(b==null) return;
        printOptimalPath(b.previousNode);
        b.printBoard();
    }

    public void printDetails(){
        System.out.println("\n\n====================\n");
        if(heuristicChoice==1) System.out.println("Hamming Distance Heuristic: \n");
        else if(heuristicChoice==2) System.out.println("Manhattan Distance Heuristic: \n");
        else System.out.println("Linear Conflict Heuristic: \n");
        System.out.println("Explored Nodes: "+exploredNodes);
        System.out.println("Expanded Nodes: "+expandedNodes);
        System.out.println("\n");
        for(int i=3;i>0;i--){
            if(i!=heuristicChoice){
                board.setHeuristicChoice(i);
                aStarSearch();

                if(i==1) System.out.println("Hamming Distance Heuristic: \n");
                else if(i==2) System.out.println("Manhattan Distance Heuristic: \n");
                else System.out.println("Linear Conflict Heuristic: \n");
                System.out.println("Explored Nodes: "+exploredNodes);
                System.out.println("Expanded Nodes: "+expandedNodes+"\n\n");
            }
        }
        System.out.println("\n====================");
    }

}


