package com.company.client;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) throws IOException {
        String host="localhost";
        Socket clientSocket_msg = new Socket(host, 6666);
        Socket clientSocket_file = new Socket(host, 7777);
        Student student = new Student(clientSocket_msg, clientSocket_file);
        student.run();
    }
}
