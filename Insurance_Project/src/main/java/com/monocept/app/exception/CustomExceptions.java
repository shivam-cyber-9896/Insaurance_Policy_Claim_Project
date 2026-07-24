package com.monocept.app.exception;

public class CustomExceptions {

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

    public static class PaymentProcessingException extends RuntimeException {
        public PaymentProcessingException(String message) {
            super(message);
        }
    }

    public static class ClaimProcessingException extends RuntimeException {
        public ClaimProcessingException(String message) {
            super(message);
        }
    }

    public static class PolicyExpiredException extends RuntimeException {
        public PolicyExpiredException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class QueryAlreadyResolvedException extends RuntimeException {
        public QueryAlreadyResolvedException(String message) {
            super(message);
        }
    }
}
