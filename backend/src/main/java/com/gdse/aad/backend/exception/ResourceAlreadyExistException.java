package com.gdse.aad.backend.exception;

// custom exception
public class ResourceAlreadyExistException extends RuntimeException{
    public ResourceAlreadyExistException(String message) {
        super(message);
    }
}
