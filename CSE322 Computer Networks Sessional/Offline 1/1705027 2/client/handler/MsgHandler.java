package com.company.client.handler;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MsgHandler extends Thread{
    private Socket msgSocket;

    private String studentID;

    private FileHandler fileHandlerObj;

    private BufferedReader inFromUser;

    private BufferedReader inFromServer;

    private PrintWriter outToServer;

    private String path, upload_reqID;

    private boolean uploadToReq = false;

    public MsgHandler(Socket msgSocket) throws IOException {
        this.msgSocket = msgSocket;
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        inFromServer = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));
        outToServer = new PrintWriter(msgSocket.getOutputStream(), true);
    }

    public void setFileHandlerObj(FileHandler fileHandlerObj) {
        this.fileHandlerObj = fileHandlerObj;
    }

    public void sentMsgToServer(String m){
        outToServer.println(m);
    }

    public void run(){
        String msg;
        try {
            System.out.println(inFromServer.readLine());
            studentID = inFromUser.readLine();
            while (studentID.equalsIgnoreCase("") || studentID.equalsIgnoreCase("\n")){
                System.out.println("plz enter student id");
                studentID = inFromUser.readLine();
                //System.out.println("in while loop : "+studentID);
            }

            outToServer.println(studentID);
            fileHandlerObj.setStudentID(studentID);

            msg = inFromServer.readLine();

            System.out.println("From server : "+msg);

            if(msg.contains("already logged in") || msg.contains("logged out")){
                System.out.println("logged out");
                msgSocket.close();
                System.exit(0);
            }


        } catch (IOException e) {
            System.err.println("exception in Msg handler - run method");
            e.printStackTrace();
        }
        while (msgSocket.isConnected()){
            try {
                Thread.sleep(500);
                System.out.println("1: Look up the list of all the students");
                System.out.println("2: Look up the list of all the files (both private and public)");
                System.out.println("3: Look up the public files of a specific student");
                System.out.println("4: Request for a file");
                System.out.println("5: View all unread messages");
                System.out.println("6: Upload a file");
                System.out.println("7: log out");

                String input = inFromUser.readLine();

                if(input.equalsIgnoreCase("1")){
                    list_of_student();
                }
                else if(input.equalsIgnoreCase("2")){
                    show_list_of_all_files(studentID);
                }
                else if(input.equalsIgnoreCase("3")){
                    show_list_of_specific_student();
                }
                else if(input.equalsIgnoreCase("4")){
                    request_for_file();
                }
                else if(input.equalsIgnoreCase("5")){
                    view_unread_msg();
                }
                else if(input.equalsIgnoreCase("6")){
                    System.out.println("Do you want to upload in a response to a request?");
                    System.out.println("1 : Yes\t\t2 : No");
                    String res = inFromUser.readLine();
                    if(res.equalsIgnoreCase("1")){
                        uploadToReq = true;
                        upload_to_request();

                    }
                    else{
                        uploadToReq = false;
                        System.out.println("Enter file directory path : ");
                        path = inFromUser.readLine();
                        send_a_file_info_to_server(path);
                    }

                }
                else if(input.equalsIgnoreCase("7")){
                    log_out_server();
                    System.exit(0);
                }
                else{
                    System.out.println("Enter a valid input!");
                }
            }
            catch (Exception e){
                System.err.println("exception in msg handler - run method while loop");
                e.printStackTrace();
            }
        }
    }

    private void view_unread_msg() throws IOException {
        outToServer.println("send unread msg");

        int cnt = Integer.parseInt(inFromServer.readLine());

        System.out.println("Unread messages:");
        if(cnt!=0) {
            for (int i = 0; i <= cnt; i++) {
                System.out.println(inFromServer.readLine());
            }
        }
    }

    private void request_for_file() throws IOException {
        String description;
        System.out.println("Enter description of your request: ");

        description = inFromUser.readLine();

        outToServer.println("request for a file");
        outToServer.println(studentID);
        outToServer.println(description);

    }

    private void upload_to_request() throws IOException {
        String res;
        System.out.println("Enter request id: ");
        res = inFromUser.readLine();
        upload_reqID = res;

        outToServer.println("sending request id for upload");
        outToServer.println(res);

        res = inFromServer.readLine();

        if(res.equalsIgnoreCase("true")){
            System.out.println("Enter file directory path : ");
            path = inFromUser.readLine();
            send_a_file_info_to_server(path);
        }
        else {
            System.out.println("invalid request id!");
        }
    }

    private void send_a_file_info_to_server(String directoryPath) throws IOException {
        File file = new File(directoryPath);

        if (!file.canRead()) {
            System.out.println("Can not open/read the file.");
            return;
        }

        String mode = "public";
        long fileLength = file.length();

        if (!uploadToReq){
            System.out.println("In which mode you want to upload the file?\n1 : public\n2 : private");
            int choice = Integer.parseInt(inFromUser.readLine());


            if (choice == 1) mode = "public";
            else if (choice == 2) mode = "private";

            System.out.println("Sending the file info to server");

            outToServer.println("Sending file info");
            outToServer.println(file.getName());
            outToServer.println(String.valueOf(fileLength));
            outToServer.println(mode);
            System.out.println("file name : "+ file.getName());
            System.out.println("file size : "+fileLength);
            System.out.println("upload mode "+mode);
        }
        else{
            outToServer.println("uploading a file in response to a request");
            outToServer.println(upload_reqID);
            outToServer.println(file.getName());
            outToServer.println(String.valueOf(fileLength));
            outToServer.println(mode);
        }

    }


    private void show_list_of_specific_student() throws IOException {
        String id, response;
        System.out.println("Enter the student id of a student:");
        id = inFromUser.readLine();

        if(id.equalsIgnoreCase("")){
            return;
        }
        if(id.equalsIgnoreCase(studentID)){
            System.out.println("you have entered your student id!");
            return;
        }
        outToServer.println("Look up the list of a specific student#"+id);

        int cnt = Integer.parseInt(inFromServer.readLine());
        for(int i=0;i<cnt;i++) System.out.println(inFromServer.readLine());
        if(cnt==0) System.out.println("Student id is invalid!");
        else{
            ask_for_download(id);
        }
    }

    private void ask_for_download(String id){
        try {
            System.out.println("Do you want to download any file?");
            System.out.println("1 : Yes\n2 : No");
            String response = inFromUser.readLine();
            //want to download file
            if (response.equalsIgnoreCase("1")) {
                System.out.println("Enter the file id : ");
                response = inFromUser.readLine();
                outToServer.println("request for file download");
                outToServer.println(id);
                outToServer.println(response);
            } else {

            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void show_list_of_all_files(String id) throws IOException {
        outToServer.println("Look up the list of all the files");

        int cnt1 = Integer.parseInt(inFromServer.readLine());
        for(int i=0;i<cnt1;i++) System.out.println(inFromServer.readLine());

        int cnt2= Integer.parseInt(inFromServer.readLine());
        for(int i=0;i<cnt2;i++) System.out.println(inFromServer.readLine());

        ask_for_download(studentID);
    }

    private void list_of_student() throws IOException {
        String line=null;
        outToServer.println("send list of all students");
        int l = Integer.parseInt(inFromServer.readLine());
        int ol = Integer.parseInt(inFromServer.readLine());
        for(int i=0;i<=l;i++) System.out.println(inFromServer.readLine());
        System.out.println();
        for(int i=0;i<=ol;i++) System.out.println(inFromServer.readLine());
    }

    private void log_out_server() throws Exception {
        outToServer.println("terminate");
        String msg = inFromServer.readLine();
        if(msg.contains("already logged in") || msg.contains("logged out")){
            System.out.println("logged out");
            msgSocket.close();
            fileHandlerObj.closeSocket();
            System.exit(0);
        }
        //Thread.sleep(500);
    }


}
