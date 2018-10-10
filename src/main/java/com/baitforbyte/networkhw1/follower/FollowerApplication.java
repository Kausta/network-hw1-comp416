package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.file.follower.FileClient;

import java.io.IOException;

public class FollowerApplication {
    private String ip;
    private int port;
    private int filePort;
    private String folderDirectory;

    public FollowerApplication(String ip, int port, int filePort, String folderDirectory) {
        this.ip = ip;
        this.port = port;
        this.filePort = filePort;
        this.folderDirectory = folderDirectory;
    }

    public void run() {
        FileClient fileClient = null;
        ConnectionToServer connectionToServer = null;
        try {
            fileClient = new FileClient(ip, filePort);
            fileClient.connect();

            connectionToServer = new ConnectionToServer(ip, port, fileClient, folderDirectory);
            connectionToServer.connect();

            // TODO: Start loop
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (connectionToServer != null) {
                    connectionToServer.disconnect();
                }
                if (fileClient != null) {
                    fileClient.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
