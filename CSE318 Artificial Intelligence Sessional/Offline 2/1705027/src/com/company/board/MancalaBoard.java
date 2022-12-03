package com.company.board;

import com.company.heuristics.Heuristic;

import java.util.ArrayList;
import java.util.Arrays;

import static com.company.play.GamePlay.*;

public class MancalaBoard implements Cloneable{
    private final int numOfBins=6;
    private final int stones=4;
    private final int numOfplayers=2;
    private int [][]board;
    private int totalStones;
    private int addtionalMoveCount;
    private int activePlayer;
    private int opponentplayer;
    private int rootPlayer;
    private int firstPlayer;
    private int player1Depth, player2Depth;
    private Heuristic heuristics[];
    private boolean bonusMove;
    private int capturedStones;

    //constructor
    public MancalaBoard() {
        board = new int[numOfplayers][numOfBins+1];
        init_board();
        heuristics = new Heuristic[2];
        addtionalMoveCount = 0;
        totalStones = numOfBins * stones * numOfplayers;
        rootPlayer = 1;
        setActivePlayer(1);
        player1Depth = 5;
        player2Depth = 5;
        bonusMove = false;
        capturedStones = 0;
    }

    //initialization
    private void init_board(){
        for(int i=0;i<numOfBins;i++){
            board[0][i] = stones;
            board[1][i] = stones;
        }
        board[0][numOfBins] = 0;
        board[1][numOfBins] = 0;
    }

    //setter


    public void setPlayer1Depth(int player1Depth) {
        this.player1Depth = player1Depth;
    }

    public void setPlayer2Depth(int player2Depth) {
        this.player2Depth = player2Depth;
    }

    public void setFirstPlayer(int firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public void setRootPlayer(int rootPlayer) {
        this.rootPlayer = rootPlayer;
    }

    void increaseAdditionalMoveCount(){
        bonusMove=true;
        addtionalMoveCount+=1;
    }

    public void setPlayer1Heuristic(Heuristic heuristic) {
        heuristics[0] = heuristic;
    }

    public void setPlayer2Heuristic(Heuristic heuristic) {
        heuristics[1] = heuristic;
    }

    public void setActivePlayer(int activePlayer) {
        this.activePlayer = activePlayer;
        this.opponentplayer = numOfplayers - activePlayer + 1;
    }

    public void setPlayer1Active(){
        setActivePlayer(1);
    }

    public void setPlayer2Active(){
        setActivePlayer(2);
    }

    //getter


    public int getCapturedStones() {
        return capturedStones;
    }

    public boolean isBonusMove() {
        return bonusMove;
    }

    public int getActivePlayerDepth(){
        if(activePlayer == player1) return player1Depth;
        else return player2Depth;
    }
    public int getPlayer1Depth() {
        return player1Depth;
    }

    public int getPlayer2Depth() {
        return player2Depth;
    }

    public int getFirstPlayer() {
        return firstPlayer;
    }

    public int getFirstPlayerStonesinStorage(){
        return board[firstPlayer-1][numOfBins];
    }

    public int getSecondPlayerStonesInStorage(){
        int opponent = (numOfplayers+1) - firstPlayer;
        return board[opponent-1][numOfBins];
    }

    public int getActivePlayer() {
        return activePlayer;
    }

    public int getOpponentplayer() {
        return opponentplayer;
    }

    public int getNumOfBins() {
        return numOfBins;
    }

    public int getNumOfplayers() {
        return numOfplayers;
    }

    public int getHeuristicValue(Stats stats){
        return heuristics[rootPlayer -1].getHeuristicval(this, stats);
    }

    public int getStonesinMystorage(){
        return board[activePlayer-1][numOfBins];
    }

    public int getStonesinOpponentStorage(){
        return board[opponentplayer-1][numOfBins];
    }


    private int getStones_on_side(int player){//player = 1 or 2
        if(player<1 || player >2){
            System.out.println("Error player number in stones on side getter(MancalaBoard class).");
            return 0;
        }

        int sum =0;
        for(int i=0;i<numOfBins;i++){
            sum+=board[player-1][i];
        }

        return sum;
    }

    public int getStones_on_my_side(){
        return getStones_on_side(activePlayer);
    }

    public int getStones_on_opponentSide(){
        return getStones_on_side(opponentplayer);
    }


    public int getAdditionalMoveCount(){
        return addtionalMoveCount;
    }

    public int numberofStones_in_a_bin(int player, int holeNum){
        //player = 1 or 2
        // holeNum = 1 to (number of holes) ==> 1-6
        if(player<1 || player >2 || holeNum<0 || holeNum>numOfBins){
            System.out.println("player id : "+player+"\t hole number: "+holeNum);
            System.out.println("Error player number or hole number in number of stones in a hole(MancalaBoard class).");
            return 0;
        }
        return board[player-1][holeNum-1];
    }


    //boolean check
    public boolean isFirstPlayer() { return  activePlayer == firstPlayer; }
    public boolean isRootPlayer(){
        return activePlayer == rootPlayer;
    }
    public boolean validBin(int player, int binNum){
        if(isEmptyBin(player, binNum)) return false;
        else return true;
    }
    public boolean isEmptyBin(int player, int holeNum){
        if(player<1 || player >2 || holeNum<0 || holeNum>numOfBins){
            System.out.println("player id : "+player+"\t hole number: "+holeNum);
            System.out.println("Error player number or hole number in isEmptyBin method(MancalaBoard class).");
            return true;
        }

        return board[player-1][holeNum-1] == 0;
    }

    public boolean isGameOver(){
        int sum;

        sum =getStones_on_side(1) ;

        if(sum==0){
            side_to_storage(2);
            //System.out.println("Sum of player 1 side : "+sum);
            return true;
        }

        sum = getStones_on_side(2);
        if(sum == 0){
            side_to_storage(1);
            //System.out.println("Sum of player 2 side : "+sum);
            return true;
        }

        return false;
    }


    //utilities
    public int make_a_move(int player, int currentHoleNum){
        //player ==> player 1 or 2
        //current hole number ==> selected hole for a move (1-6)

        if(isEmptyBin(player, currentHoleNum)){
            System.out.println("Not a valid hole. this is empty.");
            return MOVEDONE;
        }

        setActivePlayer(player); //setting who's move
        bonusMove=false;

        int stonesInHole=numberofStones_in_a_bin(player, currentHoleNum);
        boolean firstIteration=true;
        int initial_val;

        board[player-1][currentHoleNum-1]=0;//making stone count of selected hole zero

        //player 1 <<== right to left
        if(player==1){
            while(stonesInHole>0) {
                if(firstIteration) {
                    firstIteration = false;
                    initial_val=currentHoleNum-2;
                }
                else{
                    initial_val=numOfBins-1;
                }

                //adding 1 from next hole of current selected hole until stone remains
                for (int i = initial_val; i >= 0 && stonesInHole > 0; i--) {
                    board[0][i] += 1;
                    stonesInHole--;
                    if(stonesInHole == 0){
                        if(check_for_capture(player, i)){
                            return CAPTURE;
                        }
                    }
                }

                //adding 1 to storage if stone availables
                if (stonesInHole > 0) {
                    board[0][numOfBins] += 1;
                    stonesInHole--;
                    if (stonesInHole == 0) {
                        //System.out.println("Bonus turn for player 1");
                        increaseAdditionalMoveCount();
                        return BONUS_TURN;
                    }
                }

                //adding 1 to player 2 holes
                for (int i = 0; i < numOfBins && stonesInHole > 0; i++) {
                    board[1][i] += 1;
                    stonesInHole--;
                }
            }
        }
        //player 2 ==>> left to right
        else{

            while (stonesInHole>0){
                if(firstIteration) {
                    firstIteration = false;
                    initial_val=currentHoleNum;
                }
                else{
                    initial_val=0;
                }
                //adding 1 from next hole of current selected hole until stone remains
                for (int i = initial_val; i <numOfBins && stonesInHole > 0; i++) {
                    board[1][i] += 1;
                    stonesInHole--;
                    if(stonesInHole == 0){
                        if(check_for_capture(player, i)){
                            return CAPTURE;
                        }
                    }
                }

                //adding 1 to storage if stone availables
                if (stonesInHole > 0) {
                    board[1][numOfBins] += 1;
                    stonesInHole--;
                    if (stonesInHole == 0) {
                        //System.out.println("Bonus turn for player 2");
                        increaseAdditionalMoveCount();
                        return BONUS_TURN;
                    }
                }

                //adding 1 to player 1 holes
                for(int i=numOfBins-1;i>=0 && stonesInHole>0;i--){
                    board[0][i]+=1;
                    stonesInHole--;
                }
            }
        }


        setActivePlayer(opponentplayer);
        return MOVEDONE;
    }

    /*public boolean make_a_move(int player, int currentHoleNum){
        //player ==> player 1 or 2
        //current hole number ==> selected hole for a move (1-6)

        if(isEmptyBin(player, currentHoleNum)){
            System.out.println("Not a valid hole. this is empty.");
            return false;
        }

        setActivePlayer(player); //setting who's move

        int stonesInHole=numberofStones_in_a_bin(player, currentHoleNum);
        boolean firstIteration=true;
        int initial_val;

        board[player-1][currentHoleNum-1]=0;//making stone count of selected hole zero

        //player 1 <<== right to left
        if(player==1){
            while(stonesInHole>0) {
                if(firstIteration) {
                    firstIteration = false;
                    initial_val=currentHoleNum-2;
                }
                else{
                    initial_val=numOfBins-1;
                }

                //adding 1 from next hole of current selected hole until stone remains
                for (int i = initial_val; i >= 0 && stonesInHole > 0; i--) {
                    board[0][i] += 1;
                    stonesInHole--;
                    if(stonesInHole == 0){
                        check_for_capture(player, i);
                    }
                }

                //adding 1 to storage if stone availables
                if (stonesInHole > 0) {
                    board[0][numOfBins] += 1;
                    stonesInHole--;
                    if (stonesInHole == 0) {
                        //System.out.println("Bonus turn for player 1");
                        increaseAdditionalMoveCount();
                        return true;
                    }
                }

                //adding 1 to player 2 holes
                for (int i = 0; i < numOfBins && stonesInHole > 0; i++) {
                    board[1][i] += 1;
                    stonesInHole--;
                }
            }
        }
        //player 2 ==>> left to right
        else{

            while (stonesInHole>0){
                if(firstIteration) {
                    firstIteration = false;
                    initial_val=currentHoleNum;
                }
                else{
                    initial_val=0;
                }
                //adding 1 from next hole of current selected hole until stone remains
                for (int i = initial_val; i <numOfBins && stonesInHole > 0; i++) {
                    board[1][i] += 1;
                    stonesInHole--;
                    if(stonesInHole == 0){
                        check_for_capture(player, i);
                    }
                }

                //adding 1 to storage if stone availables
                if (stonesInHole > 0) {
                    board[1][numOfBins] += 1;
                    stonesInHole--;
                    if (stonesInHole == 0) {
                        //System.out.println("Bonus turn for player 2");
                        increaseAdditionalMoveCount();
                        return true;
                    }
                }

                //adding 1 to player 1 holes
                for(int i=numOfBins-1;i>=0 && stonesInHole>0;i--){
                    board[0][i]+=1;
                    stonesInHole--;
                }
            }
        }


        setActivePlayer(opponentplayer);
        return false;
    }*/

    public int getbinClosed_to_storage(int binNumber){
        if(activePlayer==player1) binNumber = (numOfBins+1)-binNumber;
        return ((numOfBins+1)-binNumber);
    }

    public int getStonesCloseToMystorage(){
        int i,j;
        int sum=0;
        for(i=0;i<numOfBins;i++){
            if(activePlayer == player1)
                sum += Math.min(board[activePlayer-1][i], i);
            else{
                j = (numOfBins - i);
                sum += Math.min(board[activePlayer-1][j], j);
            }
        }
        return sum;
    }

    public ArrayList<MancalaBoard> nextLevelChilds() {
        ArrayList<MancalaBoard> childs = new ArrayList<>(6);

        for(int i=0;i<numOfBins;i++){
            if(board[activePlayer-1][i]>0){
                MancalaBoard newMancalaBoard = null;
                try {
                    newMancalaBoard = (MancalaBoard) this.clone();
                    newMancalaBoard.addtionalMoveCount=0;
                    newMancalaBoard.make_a_move(newMancalaBoard.activePlayer, i+1);
                    newMancalaBoard.isGameOver();
                    childs.add(i, newMancalaBoard);
                } catch (CloneNotSupportedException e) {
                    System.out.println("exception in next level child method");
                    e.printStackTrace();
                }
            }
            else{
                childs.add(i, null);
            }
        }


        return childs;
    }

    public int first_valid_move(){
        int idx=-1;
        if(activePlayer == player1) {
            for (int i = 0; i < numOfBins; i++) {
                if (board[activePlayer - 1][i] > 0) {
                    idx = i;
                    break;
                }
            }
        }
        else{
            for(int i=numOfBins-1;i>=0;i--){
                if(board[activePlayer-1][i]>0){
                    idx=i;
                    break;
                }
            }
        }
        return idx;
    }


    public boolean check_for_capture(int player, int binNum){
        int opponent = (numOfplayers+1) - player;
        if(board[player-1][binNum] == 1 && board[opponent-1][binNum] != 0){
            //System.out.println("Nice move. Capture");
            capturedStones = 1+ board[opponent-1][binNum];
            board[player-1][numOfBins] += 1 + board[opponent-1][binNum];
            board[player-1][binNum] = 0;
            board[opponent-1][binNum]=0;
            return true;
        }

        return false;
    }

    private void side_to_storage(int player){
        for(int i=0;i<numOfBins;i++){
            board[player-1][numOfBins]+=board[player-1][i];
            board[player-1][i]=0;
        }
    }

    public int finalResult(){
        if(isGameOver()) {
            if(PRINTALL) {
                System.out.println("final board: ");
                print_Board();
            }
            if (board[0][numOfBins] > board[1][numOfBins]) {
                if(PRINTALL) System.out.println("Player 1 wins.");
                return 1;
            } else if (board[0][numOfBins] < board[1][numOfBins]) {
                if(PRINTALL) System.out.println("PLayer 2 wins.");
                return 2;
            } else {
                if(PRINTALL) System.out.println("It's a tie!");
                return 0;
            }
        }
        return -1;
    }

    //override
    @Override
    protected Object clone() throws CloneNotSupportedException {
        MancalaBoard cloneBoard = (MancalaBoard) super.clone();
        cloneBoard.board = new int [numOfplayers][numOfBins+1];

        for(int i=0;i<numOfplayers;i++){
            for(int j=0;j<=numOfBins;j++)
                cloneBoard.board[i][j] = board[i][j];
        }

        return cloneBoard;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null) return false;
        MancalaBoard mBoard = (MancalaBoard) obj;
        if(this == mBoard) return true;

        /*if(Arrays.deepEquals(board, mBoard.board) &&
                addtionalMoveCount == mBoard.addtionalMoveCount &&
                activePlayer == mBoard.activePlayer && maxplayer == mBoard.maxplayer) return true;*/
        if(Arrays.deepEquals(board, mBoard.board)) return true;
        return false;
    }

    //printing mancala board
    public void print_Board(){
        //index 0 - player 1
        //index 1 - player 2
        //board[i][lastholeIndex]= storage of player i+1
        //storage of player 1 is in the left most column, storage of player 2 is in the right most column

        System.out.println("\n\t\t(1)\t\t(2)\t\t(3)\t\t(4)\t\t(5)\t\t(6)\t");
        System.out.println("|=======================================================|");
        System.out.print("|\t|\t");
        for(int i=0;i<numOfBins;i++){
            System.out.print(board[0][i]+"\t|\t");
        }
        System.out.println("|");
        System.out.println("| "+board[0][numOfBins]+" |-----------------------------------------------| "+board[1][numOfBins]+" |");
        System.out.print("|\t|\t");
        for(int i=0;i<numOfBins;i++){
            System.out.print(board[1][i]+"\t|\t");
        }
        System.out.println("|");
        System.out.println("|=======================================================|");
        System.out.println("\t\t(6)\t\t(5)\t\t(4)\t\t(3)\t\t(2)\t\t(1)\t\n");
    }

}
