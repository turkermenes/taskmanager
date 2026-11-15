package dev.turkermenes.taskmanager.exception;

public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Kullanıcı adı veya şifre hatalı");
    }
}