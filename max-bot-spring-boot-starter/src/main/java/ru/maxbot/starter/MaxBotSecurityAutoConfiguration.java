package ru.maxbot.starter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.starter.internal.security.HandlerMethodAuthorizationEvaluator;
import ru.maxbot.starter.internal.security.SecurityContextHandlerInterceptor;
import ru.maxbot.starter.security.BotAuthenticationConverter;
import ru.maxbot.starter.security.DefaultBotAuthenticationConverter;

@AutoConfiguration
public class MaxBotSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public BotAuthenticationConverter botAuthenticationConverter() {
        return new DefaultBotAuthenticationConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        return new DefaultMethodSecurityExpressionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public HandlerMethodAuthorizationEvaluator handlerMethodAuthorizationEvaluator(
            MethodSecurityExpressionHandler methodSecurityExpressionHandler) {
        return new HandlerMethodAuthorizationEvaluator(methodSecurityExpressionHandler);
    }

    @Bean
    @ConditionalOnMissingBean(name = "securityContextHandlerInterceptor")
    public HandlerInterceptor securityContextHandlerInterceptor(BotAuthenticationConverter botAuthenticationConverter) {
        return new SecurityContextHandlerInterceptor(botAuthenticationConverter);
    }
}

