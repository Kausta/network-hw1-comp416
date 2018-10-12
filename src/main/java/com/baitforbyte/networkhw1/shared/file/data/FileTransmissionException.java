package com.baitforbyte.networkhw1.shared.file.data;

import java.io.IOException;

public class FileTransmissionException extends IOException {
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
}
