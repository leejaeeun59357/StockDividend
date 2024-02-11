package org.example.stockdividend.exception;

public abstract class AbstractException extends RuntimeException {

    abstract public int getStatusCode();
    abstract public String getMessage();
}
