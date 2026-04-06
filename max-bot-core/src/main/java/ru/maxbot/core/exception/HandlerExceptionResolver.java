package ru.maxbot.core.exception;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.handler.Handler;

@FunctionalInterface
public interface HandlerExceptionResolver {

    boolean resolveException(UpdateContext context, Handler handler, Exception exception);
}

