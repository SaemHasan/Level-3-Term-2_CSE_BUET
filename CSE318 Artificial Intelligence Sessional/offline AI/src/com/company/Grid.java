package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import static com.company.Main.EDGE_CUM_PROB;
import static com.company.Main.SENSOR_COR_PROB;

class AdjacentCount{
    public int cornerCount;
    public int edgeCount;

    public AdjacentCount() {
        this.cornerCount = 0;
        this.edgeCount = 0;
    }
}

public class Grid {
    private File file;
    private Scanner sc;
    private int n;
    private int m;
    private int k;
    private double edge_cum_prob, corner_cum_prob;
    private double sensor_correct_prob, sensor_wrong_prob;
    private double grid[][];
    private double evidence[][];
    private int time;

    public Grid(){
        time =0;
        initialtakeInput();
        setInitialGrid();
        edge_cum_prob = EDGE_CUM_PROB;
        corner_cum_prob = 1 - edge_cum_prob;
        sensor_correct_prob = SENSOR_COR_PROB;
        sensor_wrong_prob = 1 - sensor_correct_prob;
        printGrid();
    }

    private void initialtakeInput(){
        try {
            int row, col;
            file = new File("input.txt");
            sc = new Scanner(file);
            n = Integer.parseInt(sc.next());
            m = Integer.parseInt(sc.next());
            k = Integer.parseInt(sc.next());
            //System.out.println(n+"\t"+m+"\t"+k);
            grid = new double[n][m];
            evidence = new double[n][m];
            setToMinus();
            for(int i=0;i<k;i++){
                row = Integer.parseInt(sc.next());
                col = Integer.parseInt(sc.next());
                grid[row][col]= 0;
            }
            //printGrid();
        }
        catch (FileNotFoundException fe){
            System.out.println("File not found");
        }
    }

    public void printGrid(){
        System.out.println("\nTime : "+time);
        System.out.println("=====================");
        System.out.println("Grid : ");
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                System.out.print(String.format("%.2f",grid[i][j])+"  ");
            }
            System.out.println();
        }
        System.out.println("====================");
        System.out.println("Probabilty Sum : "+ String.format("%.0f",gridSum()));
        System.out.println("====================");
    }

    private double gridSum(){
        double sum = 0;
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                sum +=grid[i][j];
            }
        }
        return sum;
    }

    private void NormalizeGrid(){
        double TotalProbSum = 100;
        double sum = gridSum();
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                grid[i][j] = (grid[i][j]/sum)*TotalProbSum;
            }
        }
    }

    public void printEvidence(){
        System.out.println("\nEvidence :\n");
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                System.out.print(String.format("%.2f",evidence[i][j])+"  ");
            }
            System.out.println();
        }

        System.out.println();
    }


    private void setToMinus(){
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                grid[i][j]=-1;
            }
        }
    }

    private void setInitialGrid(){
        double prob = (n*m)-k;
        prob = (1/prob);
        //System.out.println("each cell prob: "+prob);
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                if(grid[i][j]!=0){
                    grid[i][j]=prob;
                }
            }
        }
        NormalizeGrid();
    }

    public void takeActionInput(){

        String action;
        int u,v,b;

        while (sc.hasNext()) {

            action = sc.next();
            //System.out.println("Action : "+action);
            if (action.equalsIgnoreCase("R")) {
                u = Integer.parseInt(sc.next());
                v = Integer.parseInt(sc.next());
                b = Integer.parseInt(sc.next());
                //System.out.println("u: "+u+"  v: "+v+"   b: "+b);
                setEvidenceMatrix(u, v, b);
                time++;
                //printEvidence();
                takeTimeStep();
                //printGrid();
                multiplyByEvidence();
                printGrid();
            }
            else if(action.equalsIgnoreCase("C")){
                findCasper();
            }
            else if(action.equalsIgnoreCase("Q")){
                break;
            }
        }

    }

    private boolean isValid(int i, int j){
        if(i<0 || i>=n) return false;
        if(j<0 || j>=m) return false;

        return true;
    }

    private boolean inEdgeRange(int i, int j, int u, int v){
        if(i<0 || i>=n) return false;
        if(j<0 || j>=m) return false;

        if((i==u) && (j==v-1 || j== v+1)){
            return true;
        }
        else if((j==v) && (i==u-1 || i==u+1)){
            return true;
        }
        return false;
    }

    private boolean inCornerRange(int i, int j, int u, int v){
        if(i<0 || i>=n) return false;
        if(j<0 || j>=m) return false;

        if(i==u && j==v) return true;
        else if(i==u-1 && j==v-1) return true;
        else if(i==u+1 && j==v+1) return true;
        else if(i==u-1 && j==v+1) return true;
        else if(i==u+1 && j==v-1) return true;

        return false;
    }

    private void setEvidenceMatrix(int u, int v, int b){
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                if(inEdgeRange(i,j,u,v) || inCornerRange(i,j,u,v)){
                    evidence[i][j] = (b==1) ? sensor_correct_prob : sensor_wrong_prob;
                }
                else{
                    evidence[i][j] = (b==0) ? sensor_correct_prob : sensor_wrong_prob;
                }
            }
        }
    }

    private AdjacentCount countCell(int u, int v){
        AdjacentCount adjacentCount = new AdjacentCount();
        for(int i=u-1;i<=u+1;i++){
            for(int j=v-1;j<=v+1;j++){
                if(isValid(i,j) && grid[i][j]!=0){
                    if(inCornerRange(i,j,u,v)) adjacentCount.cornerCount++;
                    else if(inEdgeRange(i,j,u,v)) adjacentCount.edgeCount++;
                }
            }
        }
        return adjacentCount;
    }



    private void multiplyByEvidence(){
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                grid[i][j] = grid[i][j]*evidence[i][j];
            }
        }
        NormalizeGrid();
    }

    private double calProbatTimeStep(int u, int v){
        double prob = 0;
        for(int i=u-1;i<=u+1;i++){
            for(int j=v-1;j<=v+1;j++){
                if(isValid(i,j)){
                    double d1=0, prob1=0;
                    AdjacentCount adjacentCount = countCell(i,j);
                    if(inEdgeRange(u,v,i,j)){
                        if(adjacentCount.edgeCount!=0) {
                            prob1 = (adjacentCount.cornerCount == 0 ) ? 1 : edge_cum_prob;
                            d1 = prob1 / adjacentCount.edgeCount;
                        }
                    }
                    else if(inCornerRange(u,v,i,j)){
                        if(adjacentCount.cornerCount!=0) {
                            prob1 = (adjacentCount.edgeCount==0) ? 1 : corner_cum_prob;
                            d1 = prob1 / adjacentCount.cornerCount;
                        }
                    }
                    prob += d1*grid[i][j];
                }
            }
        }
        return prob;
    }

    private void takeTimeStep(){

        double [][]calResult = new double[n][m];
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                if(grid[i][j]!=0) calResult[i][j] = calProbatTimeStep(i,j);
            }
        }
        //printGrid();
        grid = calResult;
        NormalizeGrid();
        //printGrid();
    }

    private void findCasper(){
        double maxProb=-1;
        int row=-1, col=-1;
        for(int i=0;i<n;i++){
            for(int j=0;j<m;j++){
                if(grid[i][j]>maxProb){
                    maxProb=grid[i][j];
                    row=i;
                    col=j;
                }
            }
        }

        System.out.println("====================\n");
        System.out.println("Casper is most probably at ( "+row+", "+col+" )");
        System.out.println("\n====================");
    }

}