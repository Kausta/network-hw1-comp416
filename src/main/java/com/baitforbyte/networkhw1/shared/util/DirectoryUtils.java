package com.baitforbyte.networkhw1.shared.util;

import com.baitforbyte.networkhw1.shared.file.data.ChangeTracking;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Helper class for getting and creating directories
 */
public final class DirectoryUtils {
    private DirectoryUtils() {
    }

    /**
     * Returns the user home directory
     *
     * @return User home directory
     */
    public static String getHomeDirectory() {
        return System.getProperty("user.home");
    }

    /**
     * Returns the desktop directory for the user
     *
     * @return Desktop directory
     */
    public static String getDesktopDirectory() {
        return Paths.get(getHomeDirectory(), "Desktop").toString();
    }

    /**
     * Returns a directory in the desktop, also creates it if it is absent, and creates the appropriate log files in it
     *
     * @param directory Directory to get ( and create )
     * @param mode      Whether we are server or client
     * @return Path of the directory
     */
    public static String getDirectoryInDesktop(String directory, ApplicationMode mode) {
        Path path = Paths.get(getDesktopDirectory(), directory);

        File dir = new File(path.toString());
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Cannot create folder in desktop!");
            }
        }
        try {
            ChangeTracking.createLogFiles(path.toString(), mode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path.toString();
    }
}
