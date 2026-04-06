package ru.maxbot.starter.internal.invocation;

public class BotHandlerMethodInvocationException extends RuntimeException {

    private final BotHandlerMethod handlerMethod;
    private final Exception targetException;

    public BotHandlerMethodInvocationException(BotHandlerMethod handlerMethod, String message, Exception targetException) {
        super(message, targetException);
        this.handlerMethod = handlerMethod;
        this.targetException = targetException;
    }

    public BotHandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    public Exception getTargetException() {
        return targetException;
    }
}

