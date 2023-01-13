package com.company.client.handler;

import com.company.server.Main;

import java.io.*;
import java.net.Socket;

public class FileHandler extends Thread{
    private String studentID;

    private Socket fileSocket;

    private MsgHandler msgHandlerObj;

    private BufferedReader inFromUser;

    private BufferedReader inFromServer;

    private PrintWriter outToServer;

    private String fileID,filename, uploadmode,path;
    private long chunk_size,fileSize;
    private boolean uploadToRequest = false;

    private InputStreamReader isr;
    private InputStream is;

    public FileHandler(Socket fileSocket) throws IOException {
        this.fileSocket = fileSocket;
        readerINit();
    }

    private void readerINit() throws IOException {
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
        is = fileSocket.getInputStream();
        isr = new InputStreamReader(is);
        inFromServer = new BufferedReader(isr);
        outToServer = new PrintWriter(fileSocket.getOutputStream(), true);
    }

    public void setMsgHandlerObj(MsgHandler msgHandlerObj) {
        this.msgHandlerObj = msgHandlerObj;
    }

    public void setStudentID(String studentID) {
        this.studentID = studentID;
    }


    public void run(){
        String msg;
        while (fileSocket.isConnected()){
            try {
                msg = inFromServer.readLine();
                System.out.println("From server file Socket : "+ msg);

                if(msg.equalsIgnoreCase("You can send the file.")){
                    filename = inFromServer.readLine();
                    path = filename;
                    fileID = inFromServer.readLine();
                    uploadmode = inFromServer.readLine();
                    fileSize = Long.parseLong(inFromServer.readLine());
                    chunk_size = Long.parseLong(inFromServer.readLine());

                    System.out.println("File id : "+fileID);
                    System.out.println("Chunk size : "+ chunk_size);

                    uploadToRequest = false;
                    sentfile();
                }

                else if(msg.equalsIgnoreCase("download will start soon!")){
                    filename = inFromServer.readLine();
                    String id = inFromServer.readLine();
                    chunk_size = Long.parseLong(inFromServer.readLine());

                    outToServer.println("sending download file info");
                    outToServer.println(filename);
                    outToServer.println(id);
                    outToServer.println(chunk_size);

                    receive_file(filename);

                    //System.out.println("here after receive");
                    String cmsg = inFromServer.readLine();
                    System.out.println("download confirmation msg from server : "+cmsg);
                }

                else if(msg.equalsIgnoreCase("You can send the file in a response to a request")){
                    filename = inFromServer.readLine();
                    path = filename;
                    fileID = inFromServer.readLine();
                    uploadmode = inFromServer.readLine();
                    fileSize = Long.parseLong(inFromServer.readLine());
                    chunk_size = Long.parseLong(inFromServer.readLine());

                    //System.out.println("File id : "+fileID);
                    //System.out.println("Chunk size : "+ chunk_size);
                    uploadToRequest = true;
                    sentfile();
                }

                else if(msg.equalsIgnoreCase("check now for upload")){
                    check_for_upload();
                }

            } catch (Exception e) {
                System.err.println("exception in  file handler - run while loop");
                e.printStackTrace();
            }
        }
    }


    private void check_for_upload() throws InterruptedException, IOException {
        while (true){
            //Thread.sleep(500);
            outToServer.println("check if buffer is free");
            String m = inFromServer.readLine();
            if(m.equalsIgnoreCase("No buffer is not free")) continue;
            else break;
        }
    }

    private void makeDirectory(){
        File file = new File("src\\com\\company\\client\\downloads\\"+studentID);

        // tries to create a new directory
        file.mkdirs();

        //if(value) System.out.println("New directories are created for student id : "+studentID);
    }

    private void receive_file(String filename){
        try
        {
            //System.out.println("here I am!! in received file!");
            long filesize = Long.parseLong(inFromServer.readLine());
            long chunkSize = Long.parseLong(inFromServer.readLine());
            byte[] contents = new byte[(int) chunkSize];

            makeDirectory();

            FileOutputStream fos = new FileOutputStream("src\\com\\company\\client\\downloads\\"+studentID+"\\"+filename);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            InputStream is = fileSocket.getInputStream();

            int bytesRead = 0;
            int total=0;
            long numOfChunks = (long) Math.ceil(filesize/(chunkSize*1.0));
            long receivedChunk = 0;

            while(receivedChunk!=numOfChunks)
            {
                bytesRead=is.read(contents);
                total+=bytesRead;
                bos.write(contents, 0, bytesRead);
                receivedChunk++;
                //System.out.println(total+"\t"+filesize+"\t"+receivedChunk+"\t"+numOfChunks);
            }
            bos.flush();
            fos.close();
            bos.close();

            readerINit();
        }
        catch(Exception e)
        {
            System.err.println("Could not receive the file.");
        }

    }


    private void sentfile() throws Exception{
        boolean fileSentSuccess = false;
        Thread.sleep(500);
        try
        {
            //PrintWriter outToServer = new PrintWriter(fileSocket.getOutputStream(), true);
            File file = new File(path);

            System.out.println("Sending the file to server");
            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            OutputStream os = fileSocket.getOutputStream();

            byte[] contents;
            //long fileLength = file.length();
            //outToServer.println(String.valueOf(fileLength));

            long current = 0;

            long filesize = file.length();

            long numOfChunks = ((long) Math.ceil(filesize/(chunk_size*1.0)));

            System.out.println("Number of Chunks : "+numOfChunks);

            long chunksentCount = 0;

            if(!uploadToRequest) outToServer.println("Sending the file to server");
            else{
                outToServer.println("uploading a file to a request from client");
                outToServer.println(studentID);
            }

            int p=10;
            long start;
            long end;
            float sec;

            while(chunksentCount!=numOfChunks){
                int size = (int) chunk_size;
                if(fileSize - current >= size)
                    current += size;
                else{
                    size = (int)(fileSize - current);
                    current = fileSize;
                }
                start = System.currentTimeMillis();
                contents = new byte[size];
                bis.read(contents, 0, size);
                os.write(contents);
                chunksentCount++;

                while (!(is.available()>0)){
                    end = System.currentTimeMillis();

                    sec = (end - start) / 1000F;
                    //System.out.println(sec + " seconds");

                    if(sec>30){
                        System.out.println("Timeout. Have to terminate the transmission");
                        msgHandlerObj.sentMsgToServer("Timeout. Have to terminate the transmission");
                        break;
                    }
                }

                String confirmationMsg = inFromServer.readLine();
                //System.out.println("Msg from server : "+ confirmationMsg);


                if(confirmationMsg.equalsIgnoreCase("Chunk received")) {
                    //System.out.println("Number of Chunk sent successfully : " + chunksentCount + "/" + numOfChunks);
                    int percentage = (int) ((chunksentCount/(numOfChunks*1.0))*100);
                    if(percentage>=p) {
                        p = p+10;
                        System.out.println("File sent : "+percentage+"%");
                    }
                    if(chunksentCount==numOfChunks) fileSentSuccess = true;
                }
                else{
                    System.out.println("Could not send the file");
                    break;
                }

            }
            os.flush();

            Thread.sleep(500);
            if(fileSentSuccess) {
                System.out.println("File sent successfully!");
                readerINit();
                outToServer.println("All chunks are sent successfully");
            }

        }
        catch(Exception e)
        {
            System.err.println("Could not transfer file.");
        }
    }





    /*private void sentfile(){
        boolean fileSentSuccess = false;
        try
        {
            //PrintWriter outToServer = new PrintWriter(studentSocket.getOutputStream(), true);
            File file = new File(path);

            FileInputStream fis = new FileInputStream(file);
            BufferedInputStream bis = new BufferedInputStream(fis);
            OutputStream os = fileSocket.getOutputStream();

            byte[] contents;
            long fileLength = file.length();

            int numOfChunks = Math.max((int) Math.ceil(fileLength/chunk_size),1);//check this later!

            System.out.println("Number of Chunks : "+numOfChunks);

            int chunksentCount = 0;
            long current = 0;

            while(current!=fileLength){
                System.out.println("Sending the file to server");
                outToServer.println("Sending the file to server");

                int size = (int) chunk_size;
                if(fileLength - current >= size)
                    current += size;
                else{
                    size = (int)(fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                System.out.println("content read kortesi");
                bis.read(contents, 0, size);
                os.write(contents);
                System.out.println("content pathai disi client theke");
                chunksentCount++;

                String confirmationMsg = inFromServer.readLine();
                System.out.println("Msg from server : "+ confirmationMsg);

                if(confirmationMsg.equalsIgnoreCase("Chunk received")) {
                    System.out.println("Number of Chunk sent successfully : " + chunksentCount + "/" + numOfChunks);
                    if(chunksentCount==numOfChunks) fileSentSuccess = true;
                }
                else{
                    System.out.println("Could not send the file");
                    break;
                }
            }
            if(fileSentSuccess) System.out.println("File sent successfully!");
            os.flush();
        }
        catch(Exception e)
        {
            System.err.println("Could not transfer file.");
        }
    }*/

    public void closeSocket() throws IOException {
        fileSocket.close();
    }



}
