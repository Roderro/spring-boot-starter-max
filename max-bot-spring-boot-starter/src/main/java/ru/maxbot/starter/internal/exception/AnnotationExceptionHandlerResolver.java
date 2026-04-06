package ru.maxbot.starter.internal.exception;

import ru.maxbot.starter.internal.invocation.InvocableBotHandlerMethod;
import ru.maxbot.starter.internal.invocation.BotHandlerMethodInvocationException;
import ru.maxbot.starter.internal.invocation.BotHandlerMethod;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import ru.maxbot.starter.internal.security.HandlerMethodAuthorizationEvaluator;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.handler.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;

public class AnnotationExceptionHandlerResolver implements HandlerExceptionResolver, Ordered {

    private static final Logger log = LoggerFactory.getLogger(AnnotationExceptionHandlerResolver.class);

    private final ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry;
    private final HandlerMethodArgumentResolverComposite argumentResolvers;
    private final HandlerMethodAuthorizationEvaluator authorizationEvaluator;

    public AnnotationExceptionHandlerResolver(ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
                                              HandlerMethodArgumentResolverComposite argumentResolvers,
                                              HandlerMethodAuthorizationEvaluator authorizationEvaluator) {
        this.exceptionHandlerMethodRegistry = exceptionHandlerMethodRegistry;
        this.argumentResolvers = argumentResolvers;
        this.authorizationEvaluator = authorizationEvaluator;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean resolveException(UpdateContext context, Handler handler, Exception exception) {
        if (!(exception instanceof BotHandlerMethodInvocationException invocationException)) {
            return false;
        }

        BotHandlerMethod exceptionHandler = exceptionHandlerMethodRegistry.lookupExceptionHandler(
                invocationException.getHandlerMethod(), invocationException.getTargetException());
        if (exceptionHandler == null) {
            return false;
        }

        try {
            new InvocableBotHandlerMethod(exceptionHandler, argumentResolvers, authorizationEvaluator)
                    .invokeExceptionHandler(context, invocationException.getTargetException());
            return true;
        } catch (Exception resolverException) {
            log.error("Exception handler method failed for controller={} method={}",
                    exceptionHandler.getBeanType().getName(), exceptionHandler.getMethod().getName(), resolverException);
            return false;
        }
    }
}

