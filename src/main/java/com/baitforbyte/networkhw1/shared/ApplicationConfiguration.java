package com.baitforbyte.networkhw1.shared;

import java.net.Socket;

public final class ApplicationConfiguration {
    private static volatile ApplicationConfiguration _instance = null;

    private final String fileHashType = "SHA-256";
    private final String[] localhostAddresses = new String[]{"localhost", "127.0.0.1", "::1"};
    private final String folderName = "DriveCloud";

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

    public String[] getLocalhostAddresses() {
        return localhostAddresses;
    }

    public String getFolderName() {
        return folderName;
    }

    public boolean isLocalhostAddress(Socket socket) {
        String name = socket.getInetAddress().getHostAddress();
        for (String lName : localhostAddresses) {
            if (lName.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }
}
