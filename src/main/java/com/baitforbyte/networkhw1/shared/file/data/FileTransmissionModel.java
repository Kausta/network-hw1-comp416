package com.baitforbyte.networkhw1.shared.file.data;

import java.io.Serializable;

public class FileTransmissionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String filename;
    private long length;
    private byte[] content;

    public FileTransmissionModel() {
    }

    public FileTransmissionModel(String filename, long length, byte[] content) {
        this.filename = filename;
        this.length = length;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
