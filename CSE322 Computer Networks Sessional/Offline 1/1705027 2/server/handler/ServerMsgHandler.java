package com.company.server.handler;

import com.company.server.Main;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import static com.company.server.Main.*;

public class ServerMsgHandler extends Thread{
    private boolean approved;
    private String studentID;
    private ServerFileHandler serverFileHandlerObj;
    private Socket msgSocket;

    private BufferedReader inFromClient;

    private PrintWriter outToClient;

    private Queue<String> unreadmsg = new LinkedList<>();


    public ServerMsgHandler(Socket msgSocket) throws IOException {
        this.msgSocket = msgSocket;

        inFromClient = new BufferedReader(new InputStreamReader(msgSocket.getInputStream()));

        outToClient = new PrintWriter(msgSocket.getOutputStream(),true);
    }

    public void setServerFileHandlerObj(ServerFileHandler serverFileHandlerObj) {
        this.serverFileHandlerObj = serverFileHandlerObj;
    }

    public void run(){
        String msg;
        try{

            approved = studentAuthentication();
            if(!approved) {
                msgSocket.close();
                serverFileHandlerObj.SocketClose();
                return;
            }
            else{
                System.out.println(studentID + " is logged into the server");
            }
        }
        catch (Exception e){
            System.err.println("exception in server msg handler run method");
            e.printStackTrace();
        }

        while (msgSocket.isConnected() && approved){
            try {
                msg = inFromClient.readLine();
                System.out.println(msg);

                if(msg.equalsIgnoreCase("send list of all students")){
                    sendAllStudentslist();
                }
                else if(msg.equalsIgnoreCase("Look up the list of all the files")){
                    lists_of_files_in_a_directory(studentID);
                }
                else if(msg.contains("Look up the list of a specific student")){
                    String []msgs = msg.split("#");
                    lists_of_files_in_a_directory(msgs[1]);
                }
                else if(msg.equalsIgnoreCase("Sending file info")){
                    String file_name = inFromClient.readLine();
                    long file_length = Long.parseLong(inFromClient.readLine());
                    String mode = inFromClient.readLine();
                    System.out.println("file info : \n "+ file_name +"\n"+file_length+"\n"+mode);
                    check_file_info_in_server(file_name, file_length, mode);
                }
                else if(msg.equalsIgnoreCase("request for file download")){
                    check_file_info_for_download();
                }
                else if(msg.equalsIgnoreCase("request for a file")){
                    broadcast_req_msg();
                }
                else if(msg.equalsIgnoreCase("send unread msg")){
                    send_unread_msg();
                }
                else if(msg.equalsIgnoreCase("sending request id for upload")){
                    check_req_id_from_client();
                }
                else if(msg.equalsIgnoreCase("uploading a file in response to a request")){
                    String req_id = inFromClient.readLine();
                    serverFileHandlerObj.setUpload_req_Id(req_id);
                    String file_name = inFromClient.readLine();
                    long file_length = Long.parseLong(inFromClient.readLine());
                    String mode = inFromClient.readLine();
                    System.out.println("file info : \n "+ file_name +"\n"+file_length+"\n"+mode);
                    check_upload_to_req_info(file_name, file_length, mode);
                }
                else if(msg.equalsIgnoreCase("Timeout. Have to terminate the transmission")){
                    serverFileHandlerObj.deleteFileFromServer(serverFileHandlerObj.getFilePath());
                    System.out.println("transmission terminated. chunks are deleted.");
                }

                else if(msg.equalsIgnoreCase("terminate")){
                    logoutUserfromServer();
                }
            } catch (IOException | InterruptedException e) {
                System.err.println("exception in server msg handler run method while loop");
                e.printStackTrace();
            }
        }
    }


    private void check_req_id_from_client() throws IOException {
        String reqID = inFromClient.readLine();

        if(Main.reqIdMap.containsKey(reqID)){
            outToClient.println("true");
        }
        else outToClient.println("false");
    }

    private int lineCount(String str) {
        return str.split("[\n|\r]").length;
    }

    private void send_unread_msg(){
        String msg="";
        int cnt=0;

        if(unreadmsg.size()==0){
            outToClient.println("0");
            //outToClient.println("");
            return;
        }

        while(!unreadmsg.isEmpty()){
            msg += unreadmsg.poll();
        }

        cnt = lineCount(msg);

        //System.out.println(cnt);
        //System.out.println(msg);

        outToClient.println(cnt);
        outToClient.println(msg);
    }

    private String req_id_generator(){
        String req_id="";

        req_id = "request_"+ Main.reqCount;
        Main.reqCount +=1;

        return req_id;

    }

    private void broadcast_req_msg() throws IOException {
        String id = inFromClient.readLine();
        String des = inFromClient.readLine();
        String reqID = req_id_generator();

        Main.reqIdMap.put(reqID, id);

        String msg = "Request ID : "+ reqID+"\n"+"short Description: "+des+"\n";

        for(int i=0; i<Main.students.size();i++){
            String stdid = Main.students.get(i);
            if(Main.studentlist.get(stdid).equalsIgnoreCase("ONLINE") && !id.equals(stdid)){
                Main.studentThreadMap.get(stdid).push_msg_to_queue(msg);
            }
            else if(!id.equals(stdid)){
                Queue<String> q = offlineStdMsg.get(stdid);
                q.add(msg);
                offlineStdMsg.put(stdid, q);
            }
        }
    }

    public void push_msg_to_queue(String msg){
        unreadmsg.add(msg);
    }

    private void check_file_info_for_download() throws IOException {
        String stdid, filename,file_ID;
        long chunk_size;
        stdid = inFromClient.readLine();
        file_ID = inFromClient.readLine();

        if(!fileIDMap.containsKey(file_ID)){
            System.out.println("File id is invalid");
            serverFileHandlerObj.send_msg_through_file_socket("Enter correct file id. File id is invalid.");
            return;
        }

        filename = fileIDMap.get(file_ID);

        File file = new File("src\\com\\company\\server\\Student_files\\"+stdid+"\\public\\"+filename);

        if(!file.canRead()) {
            System.out.println("Can not open/read the file.");
            serverFileHandlerObj.send_msg_through_file_socket("Can not open/read the file.");
            return;
        }

        chunk_size = MAX_CHUNK_SIZE;
        serverFileHandlerObj.send_msg_through_file_socket("download will start soon!");
        serverFileHandlerObj.send_msg_through_file_socket(filename);
        serverFileHandlerObj.send_msg_through_file_socket(stdid);
        serverFileHandlerObj.send_msg_through_file_socket(String.valueOf(chunk_size));

    }


    private void check_upload_to_req_info(String filename, long filelength, String mode) throws IOException {
        int chunk_size = (int)Math.floor(Math.random()*(MAX_CHUNK_SIZE-MIN_CHUNK_SIZE+1)+MIN_CHUNK_SIZE);

        System.out.println("file name : "+filename);
        System.out.println("file size : "+ filelength);
        System.out.println("Chunk size : "+ chunk_size);

        String fileID = fileID_generator(filename);


        if(filelength>MAX_BUFFER_SIZE){
            outToClient.println("You can't upload files larger than max buffer size.");
            return;
        }
        else if(total_buffer_in_used+filelength>MAX_BUFFER_SIZE){
            //queue te pathabo. buffer faka hoile pathanor jonno
            outToClient.println("Server buffer is full for now.");
            FileInfo fileInfo = new FileInfo(filename, fileID, mode);
            fileInfo.setFilesize(filelength);
            fileInfo.setChunk_size(chunk_size);
            serverFileHandlerObj.setFileInfo(fileInfo);
            serverFileHandlerObj.send_msg_through_file_socket("check now for upload");
            return;
        }

        total_buffer_in_used += filelength;

        serverFileHandlerObj.send_msg_through_file_socket("You can send the file in a response to a request");
        serverFileHandlerObj.send_msg_through_file_socket(filename);
        serverFileHandlerObj.send_msg_through_file_socket(fileID);
        serverFileHandlerObj.send_msg_through_file_socket(mode);
        serverFileHandlerObj.send_msg_through_file_socket(String.valueOf(filelength));
        serverFileHandlerObj.send_msg_through_file_socket(String.valueOf(chunk_size));

        serverFileHandlerObj.setFileInfo(filename, fileID, mode, filelength, chunk_size);
        serverFileHandlerObj.fileVarInit();
    }


    private void check_file_info_in_server(String filename, long filelength, String mode) throws IOException {

        int chunk_size = (int)Math.floor(Math.random()*(MAX_CHUNK_SIZE-MIN_CHUNK_SIZE+1)+MIN_CHUNK_SIZE);

        System.out.println("file name : "+filename);
        System.out.println("file size : "+ filelength);
        System.out.println("Chunk size : "+ chunk_size);

        String fileID = fileID_generator(filename);


        if(filelength>MAX_BUFFER_SIZE){
            serverFileHandlerObj.send_msg_through_file_socket("You can't upload files larger than max buffer size.");
            return;
        }
        else if(total_buffer_in_used+filelength>MAX_BUFFER_SIZE){
            //queue te pathabo. buffer faka hoile pathanor jonno
            serverFileHandlerObj.send_msg_through_file_socket("Server buffer is full for now.");
            FileInfo fileInfo = new FileInfo(filename, fileID, mode);
            fileInfo.setFilesize(filelength);
            fileInfo.setChunk_size(chunk_size);
            serverFileHandlerObj.setFileInfo(fileInfo);
            serverFileHandlerObj.send_msg_through_file_socket("check now for upload");
            return;
        }

        total_buffer_in_used += filelength;

        serverFileHandlerObj.send_msg_through_file_socket("You can send the file.");
        serverFileHandlerObj.send_msg_through_file_socket(filename);
        serverFileHandlerObj.send_msg_through_file_socket(fileID);
        serverFileHandlerObj.send_msg_through_file_socket(mode);
        serverFileHandlerObj.send_msg_through_file_socket(String.valueOf(filelength));
        serverFileHandlerObj.send_msg_through_file_socket(String.valueOf(chunk_size));

        serverFileHandlerObj.setFileInfo(filename, fileID, mode, filelength,chunk_size);
        serverFileHandlerObj.fileVarInit();
    }


    private String fileID_generator(String filename){
        String fileID = "";

        fileID = studentID+"_"+file_count;
        file_count +=1;

        fileIDMap.put(fileID, filename);
        filename = studentID+"_"+filename;
        fileName_fileIDMap.put(filename, fileID);

        return fileID;
    }

    private void lists_of_files_in_a_directory(String foldername){
        File directoryPath = new File("src\\com\\company\\server\\Student_files\\" + foldername + "\\public");
        int cnt1, cnt2;
        if(directoryPath.exists()) {
            String listsOfFiles = "Public files : \n";

            cnt1=2;
            // File directoryPath = new File("Student_files\\" + foldername + "\\public");

            File filesList[] = directoryPath.listFiles();

            for (File file : filesList) {
                listsOfFiles += "file id : "+ fileName_fileIDMap.get(foldername+"_"+file.getName())+ "\t file name : "+file.getName() + "\n";
                cnt1++;
            }
            System.out.println("Sending list of files of Student " + foldername);
            outToClient.println(cnt1);
            outToClient.println(listsOfFiles);

            if (foldername.equalsIgnoreCase(studentID)) {

                cnt2 = 2;

                listsOfFiles = "Private files : \n";

                directoryPath = new File("src\\com\\company\\server\\Student_files\\" + foldername + "\\private");

                filesList = directoryPath.listFiles();

                for (File file : filesList) {
                    listsOfFiles += "file id : "+ fileName_fileIDMap.get(foldername+"_"+file.getName())+ "\t file name : "+file.getName() + "\n";
                    cnt2++;
                }
                outToClient.println(cnt2);
                outToClient.println(listsOfFiles);
            }
        }
        else{
            System.err.println("Directory can't be found!");
            outToClient.println("0");
        }
    }

    private void sendAllStudentslist(){
        int l, ol;
        l=1;
        ol=1;
        String list="";
        String onlineList= "";
        for(int i=0; i<Main.students.size();i++){
            String id = Main.students.get(i);
            list = list + id +"\n";
            l++;
            if(Main.studentlist.get(id).equalsIgnoreCase("ONLINE")){
                onlineList += id + "\n";
                ol++;
            }
        }
//        String[] lines = list.split("|\|r|n");
//        l = lines.length;
//        lines = onlineList.split("rn|r|n");
//        ol = lines.length;
        System.out.println("list lines : "+ l);
        System.out.println("Online lines : "+ ol);
        System.out.println("Sending All students list to "+studentID+"\n");
        outToClient.println(l);
        outToClient.println(ol);
        outToClient.println("All students list : \n"+list);
        outToClient.println("Online Student List: \n"+ onlineList);
    }

    private void makeDirectory(){
        File file = new File("src\\com\\company\\server\\Student_files\\"+studentID+"\\public");
        File file1 = new File("src\\com\\company\\server\\Student_files\\"+studentID+"\\private");

        // tries to create a new directory
        boolean value = file.mkdirs();
        boolean value1 = file1.mkdirs();

        if(value && value1) System.out.println("New directories are created for student id : "+studentID);
    }

    private boolean studentAuthentication() throws IOException {

        outToClient.println("What is your student id?");
        studentID = inFromClient.readLine();
        serverFileHandlerObj.setStudentID(studentID);
        System.out.println("Student ID : "+studentID);

        if(Main.studentlist.containsKey(studentID)){
            if(Main.studentlist.get(studentID) == "OFFLINE"){ //previously logged in but offline now
                outToClient.println("Welcome to server!");
                Main.studentThreadMap.put(studentID, this);
                Main.studentlist.put(studentID, "ONLINE");

                System.out.println(offlineStdMsg.get(studentID).size());
                while (!offlineStdMsg.get(studentID).isEmpty()){
                    String m = offlineStdMsg.get(studentID).poll();
                    System.out.println(m);
                    push_msg_to_queue(m);
                }

                return true;
            }
            else{  //already logged in from another ip
                outToClient.println("You have already logged in from another ip.");
                return false;
            }
        }
        else{ //first time logged in
            outToClient.println("Welcome to server!");
            makeDirectory(); //making directory in server
            Main.students.add(studentID);
            Main.studentThreadMap.put(studentID, this);
            Main.studentlist.put(studentID, "ONLINE");
            Queue<String> q = new LinkedList<>();
            offlineStdMsg.put(studentID, q);
            return true;
        }
    }

    private void logoutUserfromServer() throws IOException, InterruptedException {
        Main.studentlist.put(studentID, "OFFLINE");
        Main.studentThreadMap.remove(studentID);
        approved = false;
        System.out.println(studentID+" is logged out from server.");
        outToClient.println("you have logged out from server");
//        if(serverFileHandlerObj.receiving_file){
//            serverFileHandlerObj.receiving_file=false;
//            total_buffer_in_used -= serverFileHandlerObj.getFile_size();
//            serverFileHandlerObj.deleteFileFromServer(serverFileHandlerObj.getFilePath());
//            System.out.println("Received chunk deleted");
//        }
        Thread.sleep(500);
        msgSocket.close();
        serverFileHandlerObj.SocketClose();
    }

}
