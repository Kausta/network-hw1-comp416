package com.baitforbyte.networkhw1.shared.file;

public class FileTransmissionException extends Exception {
    public FileTransmissionException() {
    }

    public FileTransmissionException(String message) {
        super(message);
    }

    public FileTransmissionException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileTransmissionException(Throwable cause) {
        super(cause);
    }

    public FileTransmissionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
