package com.university.tracker;

public class OpenAlexImportException extends RuntimeException {

    public OpenAlexImportException(String message) {
        super(message);
    }

    public OpenAlexImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
