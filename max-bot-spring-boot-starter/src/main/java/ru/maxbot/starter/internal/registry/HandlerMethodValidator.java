package ru.maxbot.starter.internal.registry;

import ru.maxbot.starter.internal.invocation.BotHandlerMethod;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;

public final class HandlerMethodValidator {

    private HandlerMethodValidator() {
    }

    public static void validate(BotHandlerMethod handlerMethod,
                                HandlerMethodArgumentResolverComposite argumentResolvers) {
        validate(handlerMethod, argumentResolvers, false);
    }

    public static void validateExceptionHandler(BotHandlerMethod handlerMethod,
                                HandlerMethodArgumentResolverComposite argumentResolvers) {
        validate(handlerMethod, argumentResolvers, true);
    }

    private static void validate(BotHandlerMethod handlerMethod,
                                 HandlerMethodArgumentResolverComposite argumentResolvers,
                                 boolean exceptionParameterAllowed) {
        Method method = handlerMethod.getMethod();
        if (method.getReturnType() != void.class) {
            throw new IllegalStateException("Invalid handler method " + methodId(method) + ": expected void return type");
        }

        for (MethodParameter parameter : handlerMethod.getMethodParameters()) {
            if (!exceptionParameterAllowed && Throwable.class.isAssignableFrom(parameter.getParameterType())) {
                throw new IllegalStateException(
                        "Invalid handler method " + methodId(method) + ": exception parameters are only supported " +
                                "for @MaxBotExceptionHandler methods");
            }
            if (!argumentResolvers.supportsParameter(parameter)) {
                throw new IllegalStateException(
                        "Invalid handler method " + methodId(method) + ": unsupported parameter " +
                                parameter.getParameterType().getName());
            }
        }
    }

    private static String methodId(Method method) {
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }
}


