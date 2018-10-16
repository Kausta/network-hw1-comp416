package com.baitforbyte.networkhw1.shared.util;

import com.baitforbyte.networkhw1.shared.file.data.ChangeTracking;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class DirectoryUtils {
    private DirectoryUtils() {
    }

    public static String getHomeDirectory() {
        return System.getProperty("user.home");
    }

    public static String getDesktopDirectory() {
        return Paths.get(getHomeDirectory(), "Desktop").toString();
    }

    public static String getDirectoryInDesktop(String directory) {
        Path path = Paths.get(getDesktopDirectory(), directory);

        File dir = new File(path.toString());
        if (!dir.exists()) {
            if (!dir.mkdir()) {
                throw new RuntimeException("Cannot create folder in desktop!");
            }
        }
        try {
            ChangeTracking.createLogFiles(path.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return path.toString();
    }
}
