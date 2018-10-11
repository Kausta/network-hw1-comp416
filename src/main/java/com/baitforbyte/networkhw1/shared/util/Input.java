package com.baitforbyte.networkhw1.shared.util;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.util.Scanner;

/**
 * Input helper singleton for getting and validating inputs
 */
public final class Input {
    private static Input _instance = null;
    private Scanner input;

    private Input() {
    }

    /**
     * Returns the input helper singleton instance
     *
     * @return Input helper instance
     */
    public static Input getInstance() {
        if (_instance == null) {
            synchronized (Input.class) {
                if (_instance == null) {
                    _instance = new Input();
                }
            }
        }
        return _instance;
    }

    /**
     * Gets a valid ip address
     *
     * @return Valid Ip address from user
     */
    public String getIp() {
        while (true) {
            try {
                System.out.print("IP Address: ");
                String ip = input.next();
                return validateIp(ip);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Sets the scanner for usage inside helper
     *
     * @param input Scanner
     */
    public void setScanner(Scanner input) {
        this.input = input;
    }

    /**
     * Validates ip address using InetAddress class from java
     *
     * @param ip Ip address entered by user
     * @return Validated ip address
     */
    private String validateIp(String ip) {
        if (ip == null || (ip = ip.trim()).equals("")) {
            throw new RuntimeException("You cannot leave ip empty");
        }
        try {
            // Check whether java can parse the ip
            InetAddress.getByName(ip);
            return ip;
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse ip address: " + ip);
        }
    }

    /**
     * Validates port by checking if it is out of bounds
     *
     * @param port Port number entered by the user
     * @return Validated port number
     */
    private int validatePort(int port) {
        if (port < 0 || port > 65535) {
            throw new RuntimeException("You entered an invalid port number: " + port);
        }
        return port;
    }

    /**
     * Gets the application mode as master or follower from user
     *
     * @return Application Mode
     */
    public ApplicationMode getApplicationMode() {
        ApplicationMode mode = null;
        while (mode == null) {
            System.out.print("Please choose application mode: ( [M]aster, [F]ollower ): ");
            mode = parseMode(input.nextLine());
            if (mode == null) {
                System.out.println("You entered an invalid mode.");
            }
        }
        return mode;
    }

    /**
     * Gets the port number from the user
     *
     * @return Valid port number
     */
    public int getPort(String message) {
        while (true) {
            try {
                System.out.print(message);
                int port = input.nextInt();
                return validatePort(port);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Parses entered mode by looking to lowercase version of the first character
     *
     * @param modeStr Entered mode
     * @return Parsed mode
     */
    private ApplicationMode parseMode(String modeStr) {
        if (modeStr.length() < 1)
            return null;
        switch (modeStr.toLowerCase().charAt(0)) {
            case 'm':
                return ApplicationMode.MASTER;
            case 'f':
                return ApplicationMode.FOLLOWER;
            default:
                return null;
        }
    }
}
