package ru.maxbot.starter.internal.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.maxbot.starter.internal.invocation.BotHandlerMethod;

public class HandlerMethodAuthorizationEvaluator {

    private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
            "maxbot-anonymous-key",
            "anonymousUser",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
    );

    private final MethodSecurityExpressionHandler expressionHandler;
    private final Map<Method, Expression> preAuthorizeCache = new ConcurrentHashMap<>();

    public HandlerMethodAuthorizationEvaluator(MethodSecurityExpressionHandler expressionHandler) {
        this.expressionHandler = expressionHandler;
    }

    public static HandlerMethodAuthorizationEvaluator noop() {
        return NoOpHolder.INSTANCE;
    }

    public void authorizePre(BotHandlerMethod handlerMethod, Object[] arguments) {
        Expression expression = preAuthorizeCache.computeIfAbsent(
                handlerMethod.getBridgedMethod(),
                method -> resolveExpression(handlerMethod, PreAuthorize.class)
        );
        if (expression == null) {
            return;
        }
        MethodInvocation invocation = new BotMethodInvocation(handlerMethod, arguments);
        EvaluationContext evaluationContext = expressionHandler.createEvaluationContext(authenticationSupplier(), invocation);
        Boolean granted = expression.getValue(evaluationContext, Boolean.class);
        if (!Boolean.TRUE.equals(granted)) {
            throw new AuthorizationDeniedException("Access denied by @PreAuthorize on "
                    + handlerMethod.getBeanType().getName() + "#" + handlerMethod.getMethod().getName());
        }
    }

    private Supplier<Authentication> authenticationSupplier() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null ? authentication : ANONYMOUS;
        };
    }

    private Expression resolveExpression(BotHandlerMethod handlerMethod, Class<? extends Annotation> annotationType) {
        Annotation annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBridgedMethod(), annotationType);
        if (annotation == null) {
            annotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), annotationType);
        }
        if (annotation == null) {
            return null;
        }

        String expressionString;
        if (annotation instanceof PreAuthorize preAuthorize) {
            expressionString = preAuthorize.value();
        } else {
            expressionString = null;
        }
        if (expressionString == null || expressionString.isBlank()) {
            return null;
        }
        return expressionHandler.getExpressionParser().parseExpression(expressionString);
    }

    private static final class NoOpHolder {
        private static final HandlerMethodAuthorizationEvaluator INSTANCE =
                new HandlerMethodAuthorizationEvaluator(new DefaultMethodSecurityExpressionHandler());
    }
}

