package com.baitforbyte.networkhw1.follower;

import java.io.IOException;

public class FollowerApplication {
    private String ip;
    private int port;

    public FollowerApplication(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        ConnectionToServer connectionToServer = null;
        try {
            connectionToServer = new ConnectionToServer(ip, port);
            connectionToServer.connect();

            while (true) {
                Thread.sleep(5);
            }
        } catch (IOException | InterruptedException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (connectionToServer != null) {
                    connectionToServer.disconnect();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
