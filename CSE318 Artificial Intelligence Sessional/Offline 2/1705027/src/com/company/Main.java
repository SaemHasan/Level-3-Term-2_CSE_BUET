package com.company;

import com.company.play.GamePlay;

import java.io.FileNotFoundException;

public class Main {

    public static boolean PrintAll = true;

    public static void main(String[] args) throws FileNotFoundException {
	    // write your code here
        GamePlay gamePlay = new GamePlay();
        //gamePlay.play();
        gamePlay.takeInputFromConsoleToplay();
        //gamePlay.generateStat();
        //while (true);
    }
}
