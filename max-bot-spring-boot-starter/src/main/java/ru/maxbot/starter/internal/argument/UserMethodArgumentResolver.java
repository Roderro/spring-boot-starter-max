package ru.maxbot.starter.internal.argument;

import ru.maxbot.core.model.User;
import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import org.springframework.core.MethodParameter;

public class UserMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == User.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        return context.updateContext().sender();
    }
}


