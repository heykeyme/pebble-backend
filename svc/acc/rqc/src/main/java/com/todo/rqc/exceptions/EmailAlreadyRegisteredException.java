package com.todo.rqc.exceptions;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String email) {
        super("Email '" + email + "' is already registered");
    }
}
