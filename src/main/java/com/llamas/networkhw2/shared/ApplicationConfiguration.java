package com.llamas.networkhw2.shared;

import java.net.Socket;

/**
 * Basic configuration variables for the application
 */
public final class ApplicationConfiguration {
    private static volatile ApplicationConfiguration _instance = null;

    /**
     * We use SHA-256 hash since both MD5 and SHA-1 have been broken and Java only guarantees existence of MD5, SHA-1, SHA-256
     */
    private final String fileHashType = "SHA-256";
    /**
     * Accepted localhost aliases
     */
    private final String[] localhostAddresses = new String[]{"localhost", "127.0.0.1", "::1"};
    /**
     * Folder name
     */
    private final String folderName = "DriveCloud";

    private ApplicationConfiguration() {

    }

    /**
     * Get the singleton configuration instance
     */
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

    /**
     * Checks whether the given socket was opened on the same computer
     *
     * @param socket Socket to check
     * @return Whether the socket is from localhost
     */
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
