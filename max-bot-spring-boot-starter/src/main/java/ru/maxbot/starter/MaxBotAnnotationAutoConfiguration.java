package ru.maxbot.starter;

import java.util.List;

import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.handler.Handler;
import ru.maxbot.core.state.StateStore;
import ru.maxbot.starter.internal.exception.AnnotationExceptionHandlerResolver;
import ru.maxbot.starter.internal.argument.AuthenticationMethodArgumentResolver;
import ru.maxbot.starter.internal.argument.UpdateContextMethodArgumentResolver;
import ru.maxbot.starter.internal.exception.ExceptionHandlerMethodRegistry;
import ru.maxbot.starter.internal.argument.ExceptionMethodArgumentResolver;
import ru.maxbot.starter.internal.registry.HandlerMethodRegistry;
import ru.maxbot.starter.internal.argument.MaxApiMethodArgumentResolver;
import ru.maxbot.starter.internal.adapter.AnnotationHandlerAdapter;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolver;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import ru.maxbot.starter.internal.registry.MaxBotControllerAdviceBeanPostProcessor;
import ru.maxbot.starter.internal.registry.MaxControllerBeanPostProcessor;
import ru.maxbot.starter.internal.security.HandlerMethodAuthorizationEvaluator;
import ru.maxbot.starter.internal.argument.UpdateMethodArgumentResolver;
import ru.maxbot.starter.internal.argument.UserMethodArgumentResolver;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MaxBotAnnotationAutoConfiguration {

    @Bean
    HandlerMethodRegistry handlerMethodRegistry() {
        return new HandlerMethodRegistry();
    }

    @Bean
    ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry() {
        return new ExceptionHandlerMethodRegistry();
    }

    @Bean
    HandlerMethodArgumentResolver updateContextMethodArgumentResolver() {
        return new UpdateContextMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolver updateMethodArgumentResolver() {
        return new UpdateMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolver maxApiMethodArgumentResolver() {
        return new MaxApiMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolver userMethodArgumentResolver() {
        return new UserMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolver authenticationMethodArgumentResolver() {
        return new AuthenticationMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolver exceptionMethodArgumentResolver() {
        return new ExceptionMethodArgumentResolver();
    }

    @Bean
    HandlerMethodArgumentResolverComposite handlerMethodArgumentResolverComposite(
            List<HandlerMethodArgumentResolver> resolvers) {
        return new HandlerMethodArgumentResolverComposite(resolvers);
    }

    @Bean
    Handler annotationHandlerAdapter(
            HandlerMethodRegistry handlerMethodRegistry,
            HandlerMethodArgumentResolverComposite argumentResolvers,
            ObjectProvider<HandlerMethodAuthorizationEvaluator> authorizationEvaluatorProvider,
            ObjectProvider<StateStore> stateStoreProvider) {
        return new AnnotationHandlerAdapter(
                handlerMethodRegistry,
                argumentResolvers,
                authorizationEvaluatorProvider.getIfAvailable(HandlerMethodAuthorizationEvaluator::noop),
                stateStoreProvider);
    }

    @Bean
    HandlerExceptionResolver annotationExceptionHandlerResolver(
            ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
            HandlerMethodArgumentResolverComposite argumentResolvers,
            ObjectProvider<HandlerMethodAuthorizationEvaluator> authorizationEvaluatorProvider) {
        return new AnnotationExceptionHandlerResolver(
                exceptionHandlerMethodRegistry,
                argumentResolvers,
                authorizationEvaluatorProvider.getIfAvailable(HandlerMethodAuthorizationEvaluator::noop));
    }

    @Bean
    static MaxControllerBeanPostProcessor maxControllerBeanPostProcessor(
            HandlerMethodRegistry handlerMethodRegistry,
            ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
            HandlerMethodArgumentResolverComposite argumentResolvers) {
        return new MaxControllerBeanPostProcessor(
                handlerMethodRegistry, exceptionHandlerMethodRegistry, argumentResolvers);
    }

    @Bean
    static MaxBotControllerAdviceBeanPostProcessor maxBotControllerAdviceBeanPostProcessor(
            ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
            HandlerMethodArgumentResolverComposite argumentResolvers) {
        return new MaxBotControllerAdviceBeanPostProcessor(exceptionHandlerMethodRegistry, argumentResolvers);
    }
}


