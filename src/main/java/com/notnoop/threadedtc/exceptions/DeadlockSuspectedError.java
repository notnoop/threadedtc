package com.notnoop.threadedtc.exceptions;

public class DeadlockSuspectedError extends Error {
    private static final long serialVersionUID = 8037265158235699083L;

    public DeadlockSuspectedError(String message) {
        super(message);
    }

    public DeadlockSuspectedError(Throwable cause) {
        super(cause);
    }

    public DeadlockSuspectedError(String message, Throwable cause) {
        super(message, cause);
    }
    
}
