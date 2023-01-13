package com.company.client;

import com.company.client.handler.FileHandler;
import com.company.client.handler.MsgHandler;

import java.net.Socket;

public class Student implements Runnable{

    private Socket studentSocket_msg;
    private Socket studentSocket_file;

    public Student(Socket studentSocket_msg, Socket studentSocket_file) {
        this.studentSocket_msg = studentSocket_msg;
        this.studentSocket_file = studentSocket_file;
    }

    @Override
    public void run() {


        try {
            MsgHandler msgHandler_obj = new MsgHandler(studentSocket_msg);
            msgHandler_obj.start();

            FileHandler fileHandler_obj = new FileHandler(studentSocket_file);
            fileHandler_obj.start();

            msgHandler_obj.setFileHandlerObj(fileHandler_obj);
            fileHandler_obj.setMsgHandlerObj(msgHandler_obj);

            msgHandler_obj.join();
            fileHandler_obj.join();
        }
        catch (Exception e){
            System.err.println("exception here in student class - run method");
            e.printStackTrace();
        }
    }
}
