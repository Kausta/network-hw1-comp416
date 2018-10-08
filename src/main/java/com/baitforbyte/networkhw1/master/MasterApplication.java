package com.baitforbyte.networkhw1.master;

public class MasterApplication {
    private int port;

    public MasterApplication(int port) {
        this.port = port;
    }

    public void run() {
        Server server = new Server(port);
    }
}
