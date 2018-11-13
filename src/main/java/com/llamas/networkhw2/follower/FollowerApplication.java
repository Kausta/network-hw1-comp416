package com.llamas.networkhw2.follower;

import com.llamas.networkhw2.shared.util.ConnectionMode;

import java.io.IOException;

public class FollowerApplication {
    private String ip;
    private int port;
    private ConnectionMode mode;

    public FollowerApplication(String ip, int port, ConnectionMode mode) {
        this.ip = ip;
        this.port = port;
        this.mode = mode;
    }

    public void run() {
        switch(mode) {
            case SSL:
                runSSLClient();
                break;
            case TCP:
                runTCPClient();
                break;
        }

    }

    private void runSSLClient() {
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

    private void runTCPClient() {
        TCPConnectionToServer connectionToServer = null;
        try {
            connectionToServer = new TCPConnectionToServer(ip, port);
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
