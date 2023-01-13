package com.company.server.handler;

public class FileInfo {
    private String filename;
    private String fileID;
    private String uploadMode;
    private String reqID;
    private long filesize;
    private String path;
    private long chunk_size;

    public long getChunk_size() {
        return chunk_size;
    }

    public void setChunk_size(long chunk_size) {
        this.chunk_size = chunk_size;
    }

    public FileInfo() {
    }

    public FileInfo(String filename, String fileID, String uploadMode) {
        this.filename = filename;
        this.fileID = fileID;
        this.uploadMode = uploadMode;
    }

    public FileInfo(String filename, String fileID, String uploadMode, String reqID, long filesize) {
        this.filename = filename;
        this.fileID = fileID;
        this.uploadMode = uploadMode;
        this.reqID = reqID;
        this.filesize = filesize;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getFileID() {
        return fileID;
    }

    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(String uploadMode) {
        this.uploadMode = uploadMode;
    }

    public String getReqID() {
        return reqID;
    }

    public void setReqID(String reqID) {
        this.reqID = reqID;
    }

    public long getFilesize() {
        return filesize;
    }

    public void setFilesize(long filesize) {
        this.filesize = filesize;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
