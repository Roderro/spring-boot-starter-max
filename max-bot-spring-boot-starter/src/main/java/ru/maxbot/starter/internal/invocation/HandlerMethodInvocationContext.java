package ru.maxbot.starter.internal.invocation;

import ru.maxbot.core.UpdateContext;

public record HandlerMethodInvocationContext(UpdateContext updateContext, Exception exception) {
}

