package com.baitforbyte.networkhw1.master;

import com.baitforbyte.networkhw1.shared.file.master.FileServer;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class MasterApplication {
    private int port;
    private int filePort;
    private String directoryPath;

    public MasterApplication(int port, int filePort) {
        this.port = port;
        this.filePort = filePort;
    }

    public void run() throws GeneralSecurityException {
        FileServer fsServer = null;
        Thread fsThread;
        Server server = null;
        try {
            fsServer = new FileServer(filePort);
            fsThread = new Thread(new FileServerRunnable(fsServer));
            fsThread.start();
            server = new Server(port, fsServer, filePort);
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

    private static class FileServerRunnable implements Runnable {
        private final FileServer fileServer;

        private FileServerRunnable(FileServer fileServer) {
            this.fileServer = fileServer;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    fileServer.listenAndAccept();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("Error occurred while opening connection, waiting for next connection");
                }
            }
        }
    }
}
