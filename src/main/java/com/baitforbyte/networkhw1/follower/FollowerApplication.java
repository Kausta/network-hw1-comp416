package com.baitforbyte.networkhw1.follower;

public class FollowerApplication {
    private String ip;
    private int port;

    public FollowerApplication(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        ConnectionToServer connectionToServer = new ConnectionToServer(ip, port);
        connectionToServer.connect();

        connectionToServer.disconnect();
    }
}
