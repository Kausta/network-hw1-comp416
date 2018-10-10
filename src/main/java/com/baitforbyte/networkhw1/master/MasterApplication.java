package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.shared.file.master.FileServer;

import java.io.IOException;

public class MasterApplication {
    private int port;
    private int filePort;
    private String directoryPath;
    public MasterApplication(int port, int filePort, String directoryPath) {
        this.port = port;
        this.filePort = filePort;
        this.directoryPath = directoryPath;
    }

    public void run() {
        FileServer fsServer = null;
        Server server = null;
        try {
            fsServer = new FileServer(filePort);
            server = new Server(port, fsServer, directoryPath);
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
