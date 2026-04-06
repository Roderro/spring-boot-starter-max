package ru.maxbot.core.exception;

public class MaxBotException extends RuntimeException {

    public MaxBotException(String message) {
        super(message);
    }

    public MaxBotException(String message, Throwable cause) {
        super(message, cause);
    }
}


