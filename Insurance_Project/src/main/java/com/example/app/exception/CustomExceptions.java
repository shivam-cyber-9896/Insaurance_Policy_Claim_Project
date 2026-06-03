package com.example.app.exception;

public class CustomExceptions {

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class DuplicateResourceException extends RuntimeException {
        public DuplicateResourceException(String message) {
            super(message);
        }
    }

    public static class InvalidCredentialsException extends RuntimeException {
        public InvalidCredentialsException(String message) {
            super(message);
        }
    }

    public static class InactiveUserException extends RuntimeException {
        public InactiveUserException(String message) {
            super(message);
        }
    }
}
