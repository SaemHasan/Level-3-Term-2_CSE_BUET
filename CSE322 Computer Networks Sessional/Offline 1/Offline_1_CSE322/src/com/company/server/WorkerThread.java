package com.company.server;

import java.net.Socket;

public class WorkerThread implements Runnable{

    private Socket connectionSocket;

    public WorkerThread(Socket connectSocket){
        this.connectionSocket = connectSocket;
    }

    @Override
    public void run() {

        while (connectionSocket.isConnected()){

        }
    }
}
