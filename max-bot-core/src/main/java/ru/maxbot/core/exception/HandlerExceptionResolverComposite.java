package ru.maxbot.core.exception;

import java.util.List;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.handler.Handler;

public final class HandlerExceptionResolverComposite implements HandlerExceptionResolver {

    private final List<HandlerExceptionResolver> resolvers;

    public HandlerExceptionResolverComposite(List<HandlerExceptionResolver> resolvers) {
        this.resolvers = resolvers != null ? List.copyOf(resolvers) : List.of();
    }

    @Override
    public boolean resolveException(UpdateContext context, Handler handler, Exception exception) {
        for (HandlerExceptionResolver resolver : resolvers) {
            if (resolver.resolveException(context, handler, exception)) {
                return true;
            }
        }
        return false;
    }
}

