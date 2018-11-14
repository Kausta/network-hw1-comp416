package com.llamas.networkhw2.master;

import com.llamas.networkhw2.shared.util.ConnectionMode;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MasterApplication {
    private int port;
    private ConnectionMode mode;

    public MasterApplication(int port, ConnectionMode mode) {
        this.port = port;
        this.mode = mode;
    }

    public void run() throws GeneralSecurityException {
        switch(mode) {
            case SSL:
                runSSLServer();
                break;
            case TCP:
                runTCPServer();
                break;
        }
    }

    private void runSSLServer() throws GeneralSecurityException {
        SSLServer server = null;
        try {
            server = new SSLServer(port);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Cannot open the sockets, exiting");
            return;
        }
        while (true) {
            try {
                server.listenAndAccept();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error occurred while opening connection, waiting for next connection");
            }
        }
    }

    private void runTCPServer() throws GeneralSecurityException {
        TCPServer server = null;
        try {
            server = new TCPServer(port);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Cannot open the sockets, exiting");
            return;
        }
        while (true) {
            try {
                server.listenAndAccept();
            } catch (IOException ex) {
                ex.printStackTrace();
                System.out.println("Error occurred while opening connection, waiting for next connection");
            }
        }
    }
}
