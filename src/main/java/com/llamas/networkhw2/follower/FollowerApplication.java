package com.llamas.networkhw2.follower;

import java.io.IOException;

public class FollowerApplication {
    private String ip;
    private int port;

    public FollowerApplication(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public void run() {
        SSLConnectionToServer connectionToServer = null;
        try {
            connectionToServer = new SSLConnectionToServer(ip, port);
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
