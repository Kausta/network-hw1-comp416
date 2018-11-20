package com.llamas.networkhw2.shared.base;// echo server

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.llamas.networkhw2.master.ServerThread;

public abstract class BaseServer {
    protected ServerSocket serverSocket;
    protected String sucessfullConnectionMessage, serverType;
    
    
    /**
     * Initiates a server socket on the input port
     *
     * @param port Server port
     */
    public BaseServer(int port, String message, String serverType) throws IOException {
        sucessfullConnectionMessage = message;
        this.serverType = serverType;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new IOException("Cannot open a socket on port " + port, e);
        }
    }

    public void finishInitializing(int port) throws IOException {
        System.out.println(serverType + " server is up and running on port " + port);
        System.out.println("--------------------");
        System.out.println();
        while(true) {
        listenAndAccept();
        }
    }

    public abstract void listenAndAccept() throws IOException;
    
}

