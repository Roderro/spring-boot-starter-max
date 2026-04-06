package ru.maxbot.core.interceptor;

import ru.maxbot.core.UpdateContext;

public interface HandlerInterceptor {

    default boolean preHandle(UpdateContext ctx) {
        return true;
    }

    default void postHandle(UpdateContext ctx) {
    }

    default void afterCompletion(UpdateContext ctx, Exception ex) {
    }
}


