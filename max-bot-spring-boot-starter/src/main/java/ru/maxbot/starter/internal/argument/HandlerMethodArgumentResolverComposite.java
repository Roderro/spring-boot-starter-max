package ru.maxbot.starter.internal.argument;

import ru.maxbot.starter.internal.invocation.HandlerMethodInvocationContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.MethodParameter;

public class HandlerMethodArgumentResolverComposite implements HandlerMethodArgumentResolver {

    private final List<HandlerMethodArgumentResolver> resolvers;
    private final Map<MethodParameter, HandlerMethodArgumentResolver> resolverCache =
            new ConcurrentHashMap<>(64);

    public HandlerMethodArgumentResolverComposite(List<HandlerMethodArgumentResolver> resolvers) {
        this.resolvers = new ArrayList<>(resolvers);
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return getArgumentResolver(parameter) != null;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, HandlerMethodInvocationContext context) {
        HandlerMethodArgumentResolver resolver = getArgumentResolver(parameter);
        if (resolver == null) {
            throw new IllegalStateException("No argument resolver for parameter " + parameter);
        }
        return resolver.resolveArgument(parameter, context);
    }

    private HandlerMethodArgumentResolver getArgumentResolver(MethodParameter parameter) {
        HandlerMethodArgumentResolver resolver = resolverCache.get(parameter);
        if (resolver != null) {
            return resolver;
        }

        for (HandlerMethodArgumentResolver candidate : resolvers) {
            if (candidate.supportsParameter(parameter)) {
                resolverCache.put(parameter, candidate);
                return candidate;
            }
        }
        return null;
    }
}


