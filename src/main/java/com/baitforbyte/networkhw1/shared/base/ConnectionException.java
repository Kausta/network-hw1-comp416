package com.baitforbyte.networkhw1.shared.base;

import java.io.IOException;

public class ConnectionException extends IOException {
    private static final long serialVersionUID = 1L;

    public ConnectionException() {
    }

    public ConnectionException(String message) {
        super(message);
    }

    public ConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConnectionException(Throwable cause) {
        super(cause);
    }
}
