package com.baitforbyte.networkhw1.shared.file;

import java.io.Serializable;

public class FileTransmissionModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private long length;
    private byte[] content;

    public FileTransmissionModel() {
    }

    public FileTransmissionModel(long length, byte[] content) {
        this.length = length;
        this.content = content;
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
