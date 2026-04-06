package ru.maxbot.starter.internal.argument;

import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import org.springframework.core.MethodParameter;
import ru.maxbot.core.UpdateContext;

public class UpdateContextMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == UpdateContext.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        return context.updateContext();
    }
}


