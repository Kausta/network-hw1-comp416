package com.baitforbyte.networkhw1;

import com.baitforbyte.networkhw1.follower.FollowerApplication;
import com.baitforbyte.networkhw1.master.MasterApplication;
import com.baitforbyte.networkhw1.shared.util.ApplicationMode;
import com.baitforbyte.networkhw1.shared.util.ConnectionMode;
import com.baitforbyte.networkhw1.shared.util.Input;

import java.security.GeneralSecurityException;
import java.util.Scanner;

/**
 * DriveCloud Application Entry Point
 */
public class EntryPoint {
    private Scanner input;
    private String[] args;

    /**
     * EntryPoint Constructor
     * <p>
     * Creates the input scanner and sets it to Input helper
     */
    public EntryPoint(String[] args) {
        input = new Scanner(System.in);
        Input.getInstance().setScanner(input);
        this.args = args;
    }

    /**
     * Main method creating and running EntryPoint class
     *
     * @param args Command line args, none for now
     * @throws GeneralSecurityException
     */
    public static void main(String[] args) throws GeneralSecurityException {
        EntryPoint app = new EntryPoint(args);
        try {
            app.run();
        } finally {
            app.getInput().close();
        }
    }

    /**
     * Gets the application mode and runs runMaster or runFollower
     *
     * @throws GeneralSecurityException
     */
    public void run() throws GeneralSecurityException {
        System.out.println("===================================");
        System.out.println("===  Welcome to Llama Protocol  ===");
        System.out.println("===  We secure your connection  ===");
        System.out.println("===================================");
        System.out.println();
        ApplicationMode mode = Input.getInstance().getApplicationMode(args.length > 0 ? args[0] : null);
        ConnectionMode connectionMode = Input.getInstance().getConnectionMode(args.length > 0 ? args[0] : null);

        System.out.println("\nStarting in " + mode.name().toLowerCase() + " application mode with the " + connectionMode.name().toLowerCase() + " connection mode ...");
        /*
        switch (mode) {
            case MASTER:
                this.runMaster();
                break;
            case FOLLOWER:
                this.runFollower();
                break;
        }*/
    }

    public Scanner getInput() {
        return input;
    }

    /**
     * Runs the master application by getting a port
     *
     * @throws GeneralSecurityException
     */
    public void runMaster() throws GeneralSecurityException {
        int port;
        int filePort;
        if (args.length > 2) {
            port = Integer.parseInt(args[1]);
            filePort = Integer.parseInt(args[2]);
        } else {
            port = Input.getInstance().getPort("Port number: ");
            filePort = Input.getInstance().getPort("File transmission port number: ");
        }
        System.out.println("Starting in port: " + port);

        MasterApplication application = new MasterApplication(port, filePort);
        application.run();
    }

    /**
     * Runs the follower application by getting an ip and a port
     */
    public void runFollower() {
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

        FollowerApplication application = new FollowerApplication(ip, port);
        application.run();
    }
}
