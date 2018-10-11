package com.baitforbyte.networkhw1.shared.file.data;

import com.baitforbyte.networkhw1.shared.ApplicationConfiguration;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class FileTransmissionModel implements Serializable {
    private static final long serialVersionUID = 2L;

    private String filename;
    private long length;
    private byte[] content;
    private long lastModifiedTimestamp;

    public FileTransmissionModel() {
    }

    public FileTransmissionModel(String filename, long length, byte[] content, long lastModifiedTimestamp) {
        this.filename = filename;
        this.length = length;
        this.content = content;
        this.lastModifiedTimestamp = lastModifiedTimestamp;
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

    public long getLastModifiedTimestamp() {
        return lastModifiedTimestamp;
    }

    public void setLastModifiedTimestamp(long lastModifiedTimestamp) {
        this.lastModifiedTimestamp = lastModifiedTimestamp;
    }

    /**
     * Get hash code of the file using the hash type from ApplicationConfiguration
     *
     * @return File Hash Code
     * @throws NoSuchAlgorithmException When the algorithm is not found, note that MD5, SHA-1 and SHA-256 are always found
     */
    public String getHash() throws NoSuchAlgorithmException {
        final MessageDigest md = MessageDigest.getInstance(ApplicationConfiguration.getInstance().getFileHashType());
        final byte[] hash = md.digest(content);
        return Base64.getEncoder().encodeToString(hash);
    }
}

