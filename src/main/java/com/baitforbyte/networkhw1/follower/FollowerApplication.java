package com.baitforbyte.networkhw1.follower;

import com.baitforbyte.networkhw1.shared.file.FileClient;

public class FollowerApplication {
    private String ip;
    private int port;
    private int filePort;

    public FollowerApplication(String ip, int port, int filePort) {
        this.ip = ip;
        this.port = port;
        this.filePort = filePort;
    }

    public void run() {
        FileClient fileClient = null;
        ConnectionToServer connectionToServer = null;
        try {
            fileClient = new FileClient(ip, filePort);
            fileClient.connect();

            connectionToServer = new ConnectionToServer(ip, port, fileClient);
            connectionToServer.connect();

            // TODO: Start loop
        } finally {
            if(connectionToServer != null) {
                connectionToServer.disconnect();
            }
            if(fileClient != null) {
                fileClient.disconnect();
            }
        }
    }
}
