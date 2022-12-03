package com.company.search;

import com.company.board.MancalaBoard;
import com.company.board.Stats;

import java.util.ArrayList;
import java.util.Collections;

import static com.company.play.GamePlay.PRINTALL;
import static com.company.play.GamePlay.player1;

public class DLS {
    private final static int infinity = 1000000;
    private boolean print;
    private Stats stats;

    public DLS() {
        this.print = false;
        this.stats = new Stats();
    }

    public int minimax(MancalaBoard root, int depth){
        int player = root.getActivePlayer();
        root.setRootPlayer(player);
        int i=0;
        int selectedBin=-1;

        if(depth>0) {
            Node selected = null;
            stats.initialize();
            //Node selected = alphabeta(root, depth, -infinity, infinity, root.isFirstPlayer());

            //System.out.println("Selected: ");
            //selected.getMancalaBoard().print_Board();
            ArrayList<MancalaBoard> nodes;
            nodes = root.nextLevelChilds();
            boolean moveSelected = false;

            ArrayList<Integer> arrayList = new ArrayList<>();

            for(int k=0;k<nodes.size();k++) arrayList.add(k);
            Collections.shuffle(arrayList);


            for (int k = 0; k < nodes.size(); k++) {
                i = arrayList.get(k);
                MancalaBoard child = nodes.get(i);
                if (child != null) {

                    stats.initialize();

                    Node selectedChild;

                    stats.setCapturedStones(child.getCapturedStones());
                    stats.setBinclosed_to_storage(child.getbinClosed_to_storage(i+1));
                    stats.setStonesClosed_to_storage(child.getStonesCloseToMystorage());

                    if(child.isBonusMove()) {
                        stats.increaseAdditionalMove();
                        selectedChild = alphabeta(child, depth, -infinity, infinity, child.isFirstPlayer());
                    }
                    else selectedChild = alphabeta(child, depth-1, -infinity, infinity, child.isFirstPlayer());
                    //System.out.println("Bin "+(i+1)+" : "+ selectedChild.getHeuristicval());

                    if(root.isFirstPlayer()){
                        selected = maxNode(selected, selectedChild);
                    }
                    else{
                        selected = minNode(selected, selectedChild);
                    }
                    if(selected.equals(selectedChild)){
                        selectedBin = i;
                        moveSelected = true;
                    }
                }
            }

            if (!moveSelected) {
                System.err.println("Error in minimax");
                System.out.println("cannot select any. check here in minimax");
                //System.out.println("Selected: ");
                //selected.getParent().print_Board();
                System.out.println("Selecting first valid move.");
                selectedBin = root.first_valid_move();
            }
        }
        else{
            selectedBin = root.first_valid_move();
            if(selectedBin == -1) System.err.println("Error in minimax(first valid move)");
        }
        if(player == player1 && PRINTALL)
            System.out.println("Selecting Bin : "+(selectedBin+1));
        else if(PRINTALL){
            System.out.println("Selecting Bin : "+ (root.getNumOfBins()-selectedBin));
        }
        stats.initialize();
        return selectedBin+1;
    }

    private Node alphabeta(MancalaBoard root, int depth, int alpha, int beta, boolean isMaxPlayer){
        if(depth == 0 || root.isGameOver()){
            return new Node(root, root.getHeuristicValue(stats));
        }

        int valueHeuristicVal;
        Node value = null;

        if(isMaxPlayer){
            for(MancalaBoard child : root.nextLevelChilds()){
                if(child == null) continue;
                Node nextNode;
                if(child.isBonusMove()) {
                    stats.increaseAdditionalMove();
                    nextNode = new Node(child, alphabeta(child, depth, alpha, beta, child.isFirstPlayer()).getHeuristicval());
                }
                else nextNode = new Node(child, alphabeta(child, depth - 1, alpha, beta, child.isFirstPlayer()).getHeuristicval());

                value = maxNode(value, nextNode);
                if(print){
                    System.out.println("next node: ");
                    nextNode.getParent().print_Board();
                    System.out.println("Value : ");
                    value.getParent().print_Board();
                }
                valueHeuristicVal = value.getHeuristicval();
                alpha = Math.max(alpha, valueHeuristicVal);
                if(valueHeuristicVal >= beta){
                    break;
                }
            }
        }

        else{
            for(MancalaBoard child: root.nextLevelChilds()){
                if(child == null) continue;
                Node nextNode;
                if(child.isBonusMove()) {
                    stats.increaseAdditionalMove();
                    nextNode = new Node(child, alphabeta(child, depth, alpha, beta, child.isFirstPlayer()).getHeuristicval());
                }
                else nextNode = new Node(child, alphabeta(child, depth - 1, alpha, beta, child.isFirstPlayer()).getHeuristicval());
                value = minNode(value, nextNode);
                valueHeuristicVal = value.getHeuristicval();
                beta = Math.min(beta, valueHeuristicVal);
                if(valueHeuristicVal<=alpha){
                    break;
                }
            }
        }
        //if(depth==DEPTH) System.out.println(value.getHeuristicval());
        return value;

    }

    private Node maxNode(Node b1, Node b2){
        if(b1 == null) return b2;
        if(b2 == null) return b1;

        if(b2.getHeuristicval() > b1. getHeuristicval())
            return b2;
        else
            return b1;
    }

    private Node minNode(Node b1, Node b2){
        if(b1 == null) return b2;
        if(b2 == null) return b1;

        if(b2.getHeuristicval() < b1. getHeuristicval())
            return b2;
        else
            return b1;
    }

}
