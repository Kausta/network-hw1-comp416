package com.baitforbyte.networkhw1.shared;

public final class ApplicationConfiguration {
    private static ApplicationConfiguration _instance = null;
    /**
     * SHA-1 or SHA-256 would be a better hash type,
     * however since Google Drive api uses MD5, we are going to use the same
     */
    private String fileHashType = "MD5";

    private ApplicationConfiguration() {

    }

    public static ApplicationConfiguration getInstance() {
        if (_instance == null) {
            synchronized (ApplicationConfiguration.class) {
                if (_instance == null) {
                    _instance = new ApplicationConfiguration();
                }
            }
        }
        return _instance;
    }

    public String getFileHashType() {
        return fileHashType;
    }
}
