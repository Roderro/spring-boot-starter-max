package ru.maxbot.starter.internal.argument;

import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import org.springframework.core.MethodParameter;

public class ExceptionMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Throwable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        Exception exception = context.exception();
        if (exception == null) {
            throw new IllegalStateException("No exception available for parameter " + parameter);
        }
        if (!parameter.getParameterType().isInstance(exception)) {
            throw new IllegalStateException(
                    "Exception " + exception.getClass().getName() + " is not assignable to " +
                            parameter.getParameterType().getName());
        }
        return exception;
    }
}

