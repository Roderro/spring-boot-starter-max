package ru.maxbot.starter.internal.argument;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;

public class AuthenticationMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == Authentication.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
