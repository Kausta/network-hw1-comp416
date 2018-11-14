package com.llamas.networkhw2.shared.base;// echo server

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;

public abstract class BaseServer {
    private ServerSocket serverSocket;

    /**
     * Initiates a server socket on the input port
     *
     * @param port Server port
     */
    public BaseServer(int port) throws ConnectionException {
        try {
            serverSocket = new ServerSocket(port);
            // System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
        } catch (IOException e) {
            throw new ConnectionException("Cannot open a socket on port " + port, e);
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }
}

