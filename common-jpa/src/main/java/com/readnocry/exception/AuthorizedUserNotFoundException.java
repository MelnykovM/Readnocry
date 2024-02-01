package com.readnocry.exception;

public class AuthorizedUserNotFoundException extends RuntimeException{

    public AuthorizedUserNotFoundException(String message) {
        super(message);
    }

    public AuthorizedUserNotFoundException() {
        super();
    }
}
