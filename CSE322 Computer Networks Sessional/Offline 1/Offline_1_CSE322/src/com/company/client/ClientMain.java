package com.company.client;

import java.io.IOException;
import java.net.Socket;

public class ClientMain {

    public static void main(String[] args) throws IOException {
        String host="localhost";
        Socket clientSocket = new Socket(host, 6666);
        Student student = new Student(clientSocket);
        student.run();
    }
}
