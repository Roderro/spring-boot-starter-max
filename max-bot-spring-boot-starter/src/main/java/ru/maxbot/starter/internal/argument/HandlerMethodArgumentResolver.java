package ru.maxbot.starter.internal.argument;

import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import org.springframework.core.MethodParameter;

public interface HandlerMethodArgumentResolver {

    boolean supportsParameter(MethodParameter parameter);

    Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context);
}


