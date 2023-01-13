package com.company.server;

import com.company.server.handler.ServerFileHandler;
import com.company.server.handler.ServerMsgHandler;

import java.net.Socket;

public class WorkerThread implements Runnable{

    private Socket welcomeSocket_msg;
    private Socket welcomeSocket_file;

    public WorkerThread(Socket welcomeSocket_msg, Socket welcomeSocket_file) {
        this.welcomeSocket_msg = welcomeSocket_msg;
        this.welcomeSocket_file = welcomeSocket_file;
    }

    @Override
    public void run() {

        try{
            ServerMsgHandler serverMsgHandler_obj = new ServerMsgHandler(welcomeSocket_msg);
            serverMsgHandler_obj.start();

            ServerFileHandler serverFileHandler_obj = new ServerFileHandler(welcomeSocket_file);
            serverFileHandler_obj.start();

            serverFileHandler_obj.setServerMsgHandlerObj(serverMsgHandler_obj);
            serverMsgHandler_obj.setServerFileHandlerObj(serverFileHandler_obj);

            //serverMsgHandler_obj.join();
            //serverFileHandler_obj.join();
        }
        catch (Exception e){
            System.err.println("exception in WorkerThread class- run method");
            e.printStackTrace();
        }

    }

}
