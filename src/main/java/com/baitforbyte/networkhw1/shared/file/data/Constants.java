package com.baitforbyte.networkhw1.shared.file.data;

public final class Constants {

    /**
     * Log File Constants
     */
    public static final String PREV_FILES_LOG_NAME = ".prev.veryspeciallog";
    public static final String CHANGE_FILES_LOG_NAME = ".change.veryspeciallog";
    public static final String PREV_DRIVE_LOG_NAME = ".prevdrivelogs.veryspeciallog";
    public static final String DRIVE_CHANGE_LOG_NAME = ".drivelogs.veryspeciallog";

    /**
     * Server and Client Log File Constants
     */
    public static final String[] SERVER_FILES = new String[]{PREV_FILES_LOG_NAME, CHANGE_FILES_LOG_NAME, DRIVE_CHANGE_LOG_NAME, PREV_DRIVE_LOG_NAME};
    public static final String[] CLIENT_FILES = new String[]{PREV_FILES_LOG_NAME};

    private Constants() {
    }
}
