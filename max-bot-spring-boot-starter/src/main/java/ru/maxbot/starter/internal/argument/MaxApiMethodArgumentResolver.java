package ru.maxbot.starter.internal.argument;

import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import ru.maxbot.core.api.MaxApi;
import org.springframework.core.MethodParameter;

public class MaxApiMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == MaxApi.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        return context.updateContext().api();
    }
}


