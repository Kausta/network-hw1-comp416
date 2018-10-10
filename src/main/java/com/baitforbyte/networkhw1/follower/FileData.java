package com.baitforbyte.networkhw1.follower;

public class FileData {
    public String hash;
    public long lastChangeTime;

    public FileData(String hash, long lastChangeTime) {
        this.hash = hash;
        this.lastChangeTime = lastChangeTime;
    }
}
