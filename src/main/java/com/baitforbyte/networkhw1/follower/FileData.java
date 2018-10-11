package com.baitforbyte.networkhw1.follower;

import java.io.Serializable;

public class FileData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String hash;
    private long lastChangeTime;

    public FileData() {
    }

    public FileData(String hash, long lastChangeTime) {
        this.hash = hash;
        this.lastChangeTime = lastChangeTime;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getLastChangeTime() {
        return lastChangeTime;
    }

    public void setLastChangeTime(long lastChangeTime) {
        this.lastChangeTime = lastChangeTime;
    }
}
