package com.notnoop.threadedtc.exceptions;

public class NotAllowedException extends RuntimeException {
    private static final long serialVersionUID = -213146190434136482L;

    public NotAllowedException() { super(); }
    public NotAllowedException(String s) { super(s); }
    public NotAllowedException(String s, Throwable cause) { super(s, cause); }
}
