package com.llamas.networkhw2;

import com.llamas.networkhw2.follower.FollowerApplication;
import com.llamas.networkhw2.master.MasterApplication;
import com.llamas.networkhw2.shared.util.ApplicationMode;
import com.llamas.networkhw2.shared.util.ConnectionMode;
import com.llamas.networkhw2.shared.util.Input;

import java.security.GeneralSecurityException;
import java.util.Scanner;

public class EntryPoint {
    private Scanner input;
    private String[] args;

    public EntryPoint(String[] args) {
        input = new Scanner(System.in);
        Input.getInstance().setScanner(input);
        this.args = args;
    }
    public static void main(String[] args) throws GeneralSecurityException {
        EntryPoint app = new EntryPoint(args);
        try {
            app.run();
        } finally {
            app.getInput().close();
        }
    }

    public void run() throws GeneralSecurityException {
        System.out.println("===================================");
        System.out.println("===  Welcome to Llama Protocol  ===");
        System.out.println("===  We secure your connection  ===");
        System.out.println("===================================");
        System.out.println();
        ApplicationMode mode = Input.getInstance().getApplicationMode(args.length > 0 ? args[0] : null);
        ConnectionMode connectionMode = Input.getInstance().getConnectionMode(args.length > 0 ? args[0] : null);

        System.out.println("Starting in " + mode.name().toLowerCase() + " application mode ...");
        System.out.println("Starting in " + connectionMode.name().toLowerCase() + " connection mode ...");
        switch (mode) {
            case MASTER:
                this.runMaster(connectionMode);
                break;
            case FOLLOWER:
                this.runFollower(connectionMode);
                break;
        }
    }

    public Scanner getInput() {
        return input;
    }

    public void runMaster(ConnectionMode mode) throws GeneralSecurityException {
        int port;
        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        } else {
            port = Input.getInstance().getPort("Port number: ");
        }
        System.out.println("Starting in port: " + port);

        MasterApplication application = new MasterApplication(port, mode);
        application.run();
    }

    /**
     * Runs the follower application by getting an ip and a port
     */
    public void runFollower(ConnectionMode mode) {
        String ip;
        int port;
        if (args.length > 2) {
            ip = args[1];
            port = Integer.parseInt(args[2]);
        } else {
            ip = Input.getInstance().getIp();
            port = Input.getInstance().getPort("Port number: ");
        }
        System.out.println("Connecting to " + ip + ":" + port + "");

        FollowerApplication application = new FollowerApplication(ip, port, mode);
        application.run();
    }
}
