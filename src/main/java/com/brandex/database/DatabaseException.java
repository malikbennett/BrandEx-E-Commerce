package com.brandex.database;

// Custom runtime exception to handle database errors without cluttering
// service and UI layers with checked SQLExceptions.
// This class was created with the assistance of an AI language model.
public class DatabaseException extends RuntimeException {
    // Constructor that takes a message and a cause.
    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that takes a message.
    public DatabaseException(String message) {
        super(message);
    }
}
