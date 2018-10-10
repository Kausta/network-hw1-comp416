package com.baitforbyte.networkhw1.shared.file.data;

public class FileTransmissionException extends Exception {
    private static final long serialVersionUID = 1L;

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
