package com.baitforbyte.networkhw1;

import com.baitforbyte.networkhw1.shared.ApplicationMode;

import java.util.Scanner;

public class EntryPoint {
    private Scanner input = new Scanner(System.in);

    public void run() {
        System.out.println("===  Welcome to DriveCloud   ===");
        System.out.println("===  We sync all your files  ===");
        System.out.println("================================");
        System.out.println();
        System.out.println("Please choose application mode: ( [M]aster, [F]ollower ): ");
        ApplicationMode mode = getApplicationMode();
        while(mode == null) {
            System.out.println("You entered an invalid mode.");
            mode = getApplicationMode();
        }
        System.out.println("Starting in " + (mode == ApplicationMode.MASTER ? "master" : "follower") + "mode ...");
        // TODO: Start the application
    }

    private ApplicationMode getApplicationMode() {
        System.out.println("Please choose application mode: ( [M]aster, [F]ollower ): ");
        return parseMode(input.next());
    }

    public Scanner getInput() {
        return input;
    }

    private ApplicationMode parseMode(String modeStr) {
        if(modeStr.length() < 1)
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

    public static void main(String[] args) {
        EntryPoint app = new EntryPoint();
        try {
            app.run();
        } finally {
            app.getInput().close();
        }
    }
}
