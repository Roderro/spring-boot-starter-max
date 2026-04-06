package ru.maxbot.starter.internal.security;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.starter.security.BotAuthenticationConverter;
import org.springframework.core.Ordered;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextHandlerInterceptor implements HandlerInterceptor, Ordered {

    private final BotAuthenticationConverter authenticationConverter;

    public SecurityContextHandlerInterceptor(BotAuthenticationConverter authenticationConverter) {
        this.authenticationConverter = authenticationConverter;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    public boolean preHandle(UpdateContext ctx) {
        Authentication authentication = authenticationConverter.convert(ctx);
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        return true;
    }

    @Override
    public void afterCompletion(UpdateContext ctx, Exception ex) {
        SecurityContextHolder.clearContext();
    }
}

