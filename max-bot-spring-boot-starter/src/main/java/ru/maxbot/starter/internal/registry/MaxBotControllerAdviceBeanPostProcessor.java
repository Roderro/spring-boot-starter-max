package ru.maxbot.starter.internal.registry;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import ru.maxbot.starter.internal.exception.ExceptionHandlerMethodRegistry;
import ru.maxbot.starter.internal.invocation.BotHandlerMethod;

public class MaxBotControllerAdviceBeanPostProcessor
        implements BeanPostProcessor, BeanFactoryAware, SmartInitializingSingleton {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry;
    private final HandlerMethodArgumentResolverComposite argumentResolvers;
    private BeanFactory beanFactory;

    public MaxBotControllerAdviceBeanPostProcessor(ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
                                                   HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.exceptionHandlerMethodRegistry = exceptionHandlerMethodRegistry;
        this.argumentResolvers = argumentResolvers;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        if (targetClass == null || nonAnnotatedClasses.contains(targetClass)) {
            return bean;
        }
        if (AnnotationUtils.findAnnotation(targetClass, MaxBotControllerAdvice.class) == null) {
            nonAnnotatedClasses.add(targetClass);
            return bean;
        }

        boolean registered = registerControllerAdvice(bean, targetClass);
        if (!registered) {
            nonAnnotatedClasses.add(targetClass);
        }
        return bean;
    }

    private boolean registerControllerAdvice(Object bean, Class<?> targetClass) {
        Map<Method, List<Class<? extends Throwable>>> exceptionMethods =
                ExceptionHandlerMethodIntrospector.findExceptionHandlerMethods(targetClass);
        if (exceptionMethods.isEmpty()) {
            return false;
        }

        int order = ExceptionHandlerMethodIntrospector.getOrder(bean, targetClass);
        exceptionMethods.forEach((method, exceptionTypes) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
            BotHandlerMethod handlerMethod = exceptionHandlerMethodRegistry
                    .registerControllerAdviceExceptionHandler(bean, invocableMethod, exceptionTypes, order);
            HandlerMethodValidator.validateExceptionHandler(handlerMethod, argumentResolvers);
        });
        return true;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!(beanFactory instanceof ListableBeanFactory listableBeanFactory)) {
            return;
        }
        for (String beanName : listableBeanFactory.getBeanNamesForAnnotation(MaxBotControllerAdvice.class)) {
            listableBeanFactory.getBean(beanName);
        }
        nonAnnotatedClasses.clear();
    }
}

