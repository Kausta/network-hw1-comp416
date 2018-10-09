package com.baitforbyte.networkhw1.shared.base;

import java.io.IOException;
import java.net.Socket;

public abstract class BaseClient {
    private String serverAddress;
    private int serverPort;
    private Socket s;

    /**
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public BaseClient(String address, int port) {
        serverAddress = address;
        serverPort = port;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void connect() {
        try {
            s = new Socket(serverAddress, serverPort);

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() {
        try {
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public Socket getSocket() {
        return s;
    }
}


