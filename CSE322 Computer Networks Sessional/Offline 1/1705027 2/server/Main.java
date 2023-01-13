package com.company.server;

import com.company.server.handler.ServerMsgHandler;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

public class Main {

    public static ArrayList<String> students;
    //public static ArrayList<WorkerThread> threadArrayList;
    public static HashMap<String, String> studentlist;
    public static HashMap<String, ServerMsgHandler> studentThreadMap;
    public static HashMap<String, String> fileIDMap = new HashMap<>();
    public static HashMap<String, String> fileName_fileIDMap = new HashMap<>();
    public static HashMap<String, String> reqIdMap = new HashMap<>();
    public static HashMap<String, Queue<String>> offlineStdMsg = new HashMap<>();
    public static int reqCount=1;
    public static int file_count = 1;

    public static final long MAX_BUFFER_SIZE = 1420000000;
    public static final long MIN_CHUNK_SIZE = 500;
    public static final long MAX_CHUNK_SIZE = 2000;
    public static long total_buffer_in_used =0;

    public static void main(String[] args) throws Exception{
//        System.out.println("hello world!");
        int count = 0;

        //threadArrayList = new ArrayList<>();
        studentlist = new HashMap<>();
        students = new ArrayList<>();
        studentThreadMap = new HashMap<>();

        ServerSocket welcomeSocket_msg = new ServerSocket(6666);
        ServerSocket welcomeSocket_file = new ServerSocket(7777);


        while(true){
            Socket connectionSocket_msg = welcomeSocket_msg.accept();
            Socket connectionSocket_file = welcomeSocket_file.accept();

            WorkerThread workerThread = new WorkerThread(connectionSocket_msg,connectionSocket_file);
            Thread thread = new Thread(workerThread);
            thread.start();
            //threadArrayList.add(workerThread);
            count++;
            System.out.println("New Student is connected!");
        }
        // write your code here
    }
}
