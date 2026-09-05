package com.spring.social_website.exception;

public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Current password is incorrect");
    }

}
