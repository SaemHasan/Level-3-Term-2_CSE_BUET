package com.company.server.handler;

import com.company.server.Main;

import java.io.*;
import java.net.Socket;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.security.spec.RSAOtherPrimeInfo;
import java.util.HashMap;
import java.util.Queue;

import static com.company.server.Main.*;

public class ServerFileHandler extends Thread{
    private Socket fileSocket;
    private ServerMsgHandler serverMsgHandlerObj;
    private String studentID;

    private BufferedReader inFromClient;

    private PrintWriter outToClient;

    private String file_Name, file_Id, uploadMode, upload_req_Id, filePath;

    private long file_size;
    private int chunk_size;

    private byte[] contents;
    private int total = 0;
    private FileOutputStream fos;
    private BufferedOutputStream bos;
    private InputStream is;
    private boolean sentSuccess=false, receive_file=true,fileSentSuccess;
    public boolean receiving_file=false;

    private boolean flagtoSimulate30Seconds = false;
    private boolean running = true;

    private FileInfo fileInfo;

    public ServerFileHandler(Socket fileSocket) throws IOException {
        this.fileSocket = fileSocket;

        readerInit();
    }

    private void readerInit() throws IOException {
        inFromClient = new BufferedReader(new InputStreamReader(fileSocket.getInputStream()));

        outToClient = new PrintWriter(fileSocket.getOutputStream(),true);
    }

    public void setServerMsgHandlerObj(ServerMsgHandler serverMsgHandlerObj) {
        this.serverMsgHandlerObj = serverMsgHandlerObj;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }

    public void SocketClose() throws IOException {
        running=false;
        fileSocket.close();
    }

    public void setChunk_size(int chunk_size) {
        this.chunk_size = chunk_size;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public void setUpload_req_Id(String upload_req_Id) {
        this.upload_req_Id = upload_req_Id;
    }

    public void run(){
        while (running && fileSocket.isConnected()){
            try {
                String msg = inFromClient.readLine();
                if(msg == null) {
                    System.out.println("Received null from file socket(client).");
                    break;
                }
                if(!msg.equalsIgnoreCase("check if buffer is free"))
                    System.out.println("from file socket client : "+ msg);
                if(msg.equalsIgnoreCase("Sending the file to server")){
                    receiving_file=true;
                    received();
                    receiving_file=false;
                }
                else if(msg.equalsIgnoreCase("sending download file info")){
                    sentfile();
                    readerInit();
                    //Thread.sleep(500);
                    if(fileSentSuccess){
                        System.out.println("here sending confirmation to client");
                        outToClient.println("File sent successfully");
                    }
                }
                else if(msg.equalsIgnoreCase("check if buffer is free")){
                    check_buffer_free();
                }
                else if(msg.equalsIgnoreCase("uploading a file to a request from client")){
                    System.out.println("Request ID : "+ upload_req_Id);
                    String uploaderID = inFromClient.readLine();
                    System.out.println("Student "+ uploaderID+" is uploading a file to request id "+ upload_req_Id);

                    sentSuccess=false;

                    receiving_file=true;
                    received();
                    receiving_file=false;

                    if(sentSuccess){
                        String updatemsg = "Update for request id : "+upload_req_Id+".\nStudent "+uploaderID+" has uploaded a file in response to your request.\nFile id : "+fileName_fileIDMap.get(uploaderID+"_"+file_Name)+"\n";

                        if(Main.studentThreadMap.containsKey(Main.reqIdMap.get(upload_req_Id)))
                            Main.studentThreadMap.get(Main.reqIdMap.get(upload_req_Id)).push_msg_to_queue(updatemsg);
                        else{
                            String StdID = Main.reqIdMap.get(upload_req_Id);
                            Queue<String> q =offlineStdMsg.get(StdID);
                            q.add(updatemsg);
                            offlineStdMsg.put(StdID, q);
                        }
                    }
                }
            }
            catch (IOException e){
                System.out.println("exception in server file handler while loop");
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void send_msg_through_file_socket(String msg){
        outToClient.println(msg);
    }

    public void setFileInfo(String file_Name, String file_Id, String uploadMode, long file_size, int chunk_size){
        this.file_Name = file_Name;
        this.file_Id = file_Id;
        this.uploadMode = uploadMode;
        this.file_size = file_size;
        this.chunk_size = chunk_size;
    }

    private void check_buffer_free() throws IOException {
        if(total_buffer_in_used+fileInfo.getFilesize()<MAX_BUFFER_SIZE){
            outToClient.println("Yes buffer is free");
            outToClient.println("You can send the file.");
            outToClient.println(fileInfo.getFilename());
            outToClient.println(fileInfo.getFileID());
            outToClient.println(fileInfo.getUploadMode());
            outToClient.println(String.valueOf(fileInfo.getFilesize()));
            outToClient.println(String.valueOf(fileInfo.getChunk_size()));

            setFileInfo(fileInfo.getFilename(), fileInfo.getFileID(), fileInfo.getUploadMode(), fileInfo.getFilesize(), (int) fileInfo.getChunk_size());
            fileVarInit();
        }
        else outToClient.println("No buffer is not free");
    }

    public void fileVarInit() throws IOException {
        filePath = "src\\com\\company\\server\\Student_files\\"+studentID+"\\"+uploadMode+"\\"+file_Name;
        fos = new FileOutputStream(filePath);
        bos = new BufferedOutputStream(fos);
        is = fileSocket.getInputStream();
        contents = new byte[10000];
        total =0;
        receive_file = true;
    }

    private void sentfile(){
        fileSentSuccess = false;
        try
        {
            Thread.sleep(500);
            String filename = inFromClient.readLine();
            String id = inFromClient.readLine();
            long chunk_size = Long.parseLong(inFromClient.readLine());

            File file = new File("src\\com\\company\\server\\Student_files\\"+id+"\\public\\"+filename);


            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            OutputStream os = fileSocket.getOutputStream();

            byte[] contents;
            long fileLength = file.length();
            outToClient.println(String.valueOf(fileLength));
            outToClient.println(String.valueOf(chunk_size));

            long numOfChunks = ((long) Math.ceil(fileLength/(chunk_size*1.0)));
            System.out.println("total chunks : "+numOfChunks);
            long sentChunkCount = 0;
            long current = 0;

            while(sentChunkCount!= numOfChunks){
                int size = (int) chunk_size;
                if(fileLength - current >= size)
                    current += size;
                else{
                    size = (int)(fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
                sentChunkCount++;
            }

            fileSentSuccess=true;
            os.flush();
            System.out.println("File sent successfully!");
            Thread.sleep(500);
        }
        catch(IOException|InterruptedException e)
        {
            fileSentSuccess=false;
            System.err.println("Could not transfer file.");
        }
    }


    public String getFilePath() {
        return filePath;
    }

    public void deleteFileFromServer(String path){
        try
        {
            Files.deleteIfExists(Paths.get(path));
        }
        catch(NoSuchFileException e)
        {
            System.out.println("No such file/directory exists");
        }
        catch(DirectoryNotEmptyException e)
        {
            System.out.println("Directory is not empty.");
        }
        catch(IOException e)
        {
            System.out.println("Invalid permissions.");
        }

        System.out.println("Deletion successful.");
    }


    /*private void sentfile() throws Exception{
        //boolean fileSentSuccess = false;
        //Thread.sleep(500);
        try
        {
            String filename = inFromClient.readLine();
            String id = inFromClient.readLine();
            long chunk_size = Long.parseLong(inFromClient.readLine());

            //System.out.println(filename);
            //System.out.println(id);
            //System.out.println(chunk_size);

            File file = new File("src\\com\\company\\server\\Student_files\\"+id+"\\public\\"+filename);

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            OutputStream os = fileSocket.getOutputStream();

            byte[] contents;
            long fileLength = file.length();

            long current = 0;

            System.out.println("sending file length");
            outToClient.println(fileLength);
            System.out.println("after sending file length");
            while(current!= fileLength){
                System.out.println("in while loop");
                int size = (int) chunk_size;
                if(fileLength - current >= size)
                    current += size;
                else{
                    size = (int)(fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
            }
            os.flush();
            //outToClient.println("File sent successfully!");
            System.out.println("File sent successfully!");
        }
        catch(Exception e)
        {
            //outToClient.println("Could not transfer file.");
            System.err.println("Could not transfer file.");
        }
    }*/

    public long getFile_size() {
        return file_size;
    }

    private void received(){
        try
        {
            long numOfChunks = ((long) Math.ceil(file_size/(chunk_size*1.0)));
            receiving_file = true;
            int receivedChunkCount =0;
            int bytesRead =0;
            while(receivedChunkCount!=numOfChunks)
            {
                bytesRead=is.read(contents);
                total+=bytesRead;
                bos.write(contents, 0, bytesRead);
                receivedChunkCount++;
                if(receivedChunkCount>5 && flagtoSimulate30Seconds) break;
                outToClient.println("Chunk received");
            }
            bos.flush();
            fos.close();
            bos.close();
            receiving_file = false;
            sentSuccess=true;
            total_buffer_in_used -= file_size;

            String msg = inFromClient.readLine();
            System.out.println(msg);
            if(msg.equalsIgnoreCase("All chunks are sent successfully")){
                if(total==file_size){
                    outToClient.println("All chunks received successfully.");
                }
                else {
                    outToClient.println("Error! All chunks are not received correctly.");
                }
            }
        }
        catch(IOException|ArrayIndexOutOfBoundsException e)
        {
            try {
                bos.flush();
                fos.close();
                bos.close();
            }catch (IOException exception){
                System.out.println("here in exception in exception");
            }

            receiving_file = false;
            total_buffer_in_used -= file_size;
            System.err.println("Could not receive the file.");
            deleteFileFromServer(filePath);
            System.out.println("Received chunk deleted");
        }
    }


    /*private void receiveFile(){
        try
        {
            //System.out.println("file name : "+file_Name);

            int bytesRead = 0;

            //while(total!=file_size)
            {
                System.out.println("1");
                bytesRead=is.read(contents);
                System.out.println("2");
                total+=bytesRead;
                bos.write(contents, 0, bytesRead);
                outToClient.println("Chunk received");
                System.out.println("Total : "+ total);
            }
        }
        catch(Exception e)
        {
            System.err.println("Total in exception : "+ total);
            System.err.println("Could not transfer file.");
        }
    }*/

}
