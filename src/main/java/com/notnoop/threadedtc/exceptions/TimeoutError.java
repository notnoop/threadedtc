package com.notnoop.threadedtc.exceptions;

public class TimeoutError extends Error {
    private static final long serialVersionUID = -2466084699959900402L;

    public TimeoutError(String message) {
        super(message);
    }

    public TimeoutError(Throwable cause) {
        super(cause);
    }

    public TimeoutError(String message, Throwable cause) {
        super(message, cause);
    }
    
}
