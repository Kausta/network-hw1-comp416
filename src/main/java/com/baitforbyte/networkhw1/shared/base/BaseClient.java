package com.baitforbyte.networkhw1.shared.base;

import java.io.IOException;
import java.net.Socket;

public abstract class BaseClient {
    private String serverAddress;
    private int serverPort;
    private Socket s = null;

    /**
     * Open a socket for client at address:port
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port    port number of the server
     */
    public BaseClient(String address, int port) {
        serverAddress = address;
        serverPort = port;
    }

    /**
     * Use the given socket as client socket
     *
     * @param s Socket to use as connection socket
     */
    public BaseClient(Socket s) {
        serverAddress = s.getInetAddress().getHostAddress();
        serverPort = s.getPort();
        this.s = s;
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void connect() throws IOException {
        if (s != null) {
            throw new ConnectionException("Previous socket is still open, please disconnect first");
        }
        try {
            s = new Socket(serverAddress, serverPort);

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        } catch (IOException e) {
            throw new ConnectionException("Error: no server has been found on " + serverAddress + "/" + serverPort, e);
        }
    }


    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect() throws IOException {
        try {
            s.close();
            System.out.println("BaseClient: Connection closed");
        } catch (IOException e) {
            throw new ConnectionException("Cannot close connection: " + e.getMessage(), e);
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


