package com.baitforbyte.networkhw1.shared.util;

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
        return Paths.get(getDesktopDirectory(), directory).toString();
    }
}
