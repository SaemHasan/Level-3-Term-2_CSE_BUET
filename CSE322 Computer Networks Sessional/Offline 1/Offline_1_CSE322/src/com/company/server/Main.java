package com.company.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Main {

    public static ArrayList<WorkerThread> threadArrayList;

    public static void main(String[] args) throws Exception{
//        System.out.println("hello world!");
        int count = 0;

        threadArrayList = new ArrayList<WorkerThread>();

        ServerSocket welcomeSocket = new ServerSocket(6666);


        while(true){
            Socket connectionSocket = welcomeSocket.accept();
            WorkerThread workerThread = new WorkerThread(connectionSocket);
            Thread thread = new Thread(workerThread);
            thread.start();
            threadArrayList.add(workerThread);
            count++;

            System.out.println("user "+count+" is connected!");
        }
	// write your code here
    }
}
