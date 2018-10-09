package com.baitforbyte.networkhw1.master;

public class MasterApplication {
    private int port;
    private int filePort;

    public MasterApplication(int port, int filePort) {
        this.port = port;
        this.filePort = filePort;
    }

    public void run() {
        Server server = new Server(port);
    }
}
