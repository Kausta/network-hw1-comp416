package com.baitforbyte.networkhw1;

import com.baitforbyte.networkhw1.follower.FollowerApplication;
import com.baitforbyte.networkhw1.master.MasterApplication;
import com.baitforbyte.networkhw1.shared.util.ApplicationMode;
import com.baitforbyte.networkhw1.shared.util.Input;

import java.util.Scanner;

/**
 * DriveCloud Application Entry Point
 */
public class EntryPoint {
    private Scanner input;

    /**
     * EntryPoint Constructor
     *
     * Creates the input scanner and sets it to Input helper
     */
    public EntryPoint() {
        input = new Scanner(System.in);
        Input.getInstance().setScanner(input);
    }

    /**
     * Main method creating and running EntryPoint class
     * @param args Command line args, none for now
     */
    public static void main(String[] args) {
        EntryPoint app = new EntryPoint();
        try {
            app.run();
        } finally {
            app.getInput().close();
        }
    }

    /**
     * Gets the application mode and runs runMaster or runFollower
     */
    public void run() {
        System.out.println("===  Welcome to DriveCloud   ===");
        System.out.println("===  We sync all your files  ===");
        System.out.println("================================");
        System.out.println();
        ApplicationMode mode = Input.getInstance().getApplicationMode();

        System.out.println("Starting in " + mode.name().toLowerCase() + " mode ...");
        switch (mode) {
            case MASTER:
                this.runMaster();
                break;
            case FOLLOWER:
                this.runFollower();
                break;
        }
    }

    public Scanner getInput() {
        return input;
    }

    /**
     * Runs the master application by getting a port
     */
    public void runMaster() {
        int port = Input.getInstance().getPort("Port number: ");
        int filePort = Input.getInstance().getPort("File transmission port number: ");
        System.out.println("Starting in port: " + port);

        MasterApplication application = new MasterApplication(port, filePort);
        application.run();
    }

    /**
     * Runs the follower application by getting an ip and a port
     */
    public void runFollower() {
        String ip = Input.getInstance().getIp();
        int port = Input.getInstance().getPort("Port number: ");
        int filePort = Input.getInstance().getPort("File transmission port number: ");

        System.out.println("Connecting to " + ip + ":" + "port");

        FollowerApplication application = new FollowerApplication(ip, port, filePort);
        application.run();
    }
}
