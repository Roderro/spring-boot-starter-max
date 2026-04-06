package ru.maxbot.starter.internal.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import ru.maxbot.core.model.UpdateType;
import ru.maxbot.starter.annotations.BotAddedRequest;
import ru.maxbot.starter.annotations.BotRemovedRequest;
import ru.maxbot.starter.annotations.BotStartedRequest;
import ru.maxbot.starter.annotations.CallbackRequest;
import ru.maxbot.starter.annotations.ChatTitleChangedRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageEditedRequest;
import ru.maxbot.starter.annotations.MessageRemovedRequest;
import ru.maxbot.starter.annotations.MessageRequest;
import ru.maxbot.starter.annotations.UserAddedRequest;
import ru.maxbot.starter.annotations.UserRemovedRequest;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import ru.maxbot.starter.internal.exception.ExceptionHandlerMethodRegistry;
import ru.maxbot.starter.internal.invocation.BotHandlerMethod;

public class MaxControllerBeanPostProcessor
        implements BeanPostProcessor, BeanFactoryAware, SmartInitializingSingleton {

    private final Set<Class<?>> nonAnnotatedClasses = Collections.newSetFromMap(new ConcurrentHashMap<>(64));
    private final HandlerMethodRegistry handlerMethodRegistry;
    private final ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry;
    private final HandlerMethodArgumentResolverComposite argumentResolvers;
    private BeanFactory beanFactory;

    public MaxControllerBeanPostProcessor(HandlerMethodRegistry handlerMethodRegistry,
                                          ExceptionHandlerMethodRegistry exceptionHandlerMethodRegistry,
                                          HandlerMethodArgumentResolverComposite argumentResolvers) {
        this.handlerMethodRegistry = handlerMethodRegistry;
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
        if (AnnotationUtils.findAnnotation(targetClass, MaxController.class) == null) {
            nonAnnotatedClasses.add(targetClass);
            return bean;
        }

        boolean registered = registerController(bean, targetClass);
        if (!registered) {
            nonAnnotatedClasses.add(targetClass);
        }
        return bean;
    }

    private boolean registerController(Object bean, Class<?> targetClass) {
        Map<Method, List<HandlerMethodRegistry.HandlerMethodMappingInfo>> requestMethods =
                findRequestMappingMethods(targetClass);
        requestMethods.forEach((method, mappingInfos) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
            BotHandlerMethod handlerMethod = handlerMethodRegistry.registerController(bean, invocableMethod, mappingInfos);
            HandlerMethodValidator.validate(handlerMethod, argumentResolvers);
        });

        Map<Method, List<Class<? extends Throwable>>> exceptionMethods =
                ExceptionHandlerMethodIntrospector.findExceptionHandlerMethods(targetClass);
        exceptionMethods.forEach((method, exceptionTypes) -> {
            Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
            BotHandlerMethod handlerMethod = exceptionHandlerMethodRegistry
                    .registerControllerExceptionHandler(bean, invocableMethod, exceptionTypes);
            HandlerMethodValidator.validateExceptionHandler(handlerMethod, argumentResolvers);
        });

        return !requestMethods.isEmpty() || !exceptionMethods.isEmpty();
    }

    private Map<Method, List<HandlerMethodRegistry.HandlerMethodMappingInfo>> findRequestMappingMethods(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<List<HandlerMethodRegistry.HandlerMethodMappingInfo>>) method -> {
                    List<HandlerMethodRegistry.HandlerMethodMappingInfo> infos = createMappingInfos(method);
                    return infos.isEmpty() ? null : infos;
                });
    }

    private List<HandlerMethodRegistry.HandlerMethodMappingInfo> createMappingInfos(Method method) {
        List<HandlerMethodRegistry.HandlerMethodMappingInfo> result = new ArrayList<>();

        CommandRequest onCommand = AnnotatedElementUtils.findMergedAnnotation(method, CommandRequest.class);
        if (onCommand != null) {
            result.add(new HandlerMethodRegistry.HandlerMethodMappingInfo(
                    HandlerMethodRegistry.MappingType.COMMAND,
                    "CommandRequest",
                    normalizeCommand(onCommand.value()),
                    null,
                    null,
                    onCommand.state(),
                    onCommand.order()));
        }

        MessageRequest onMessage = AnnotatedElementUtils.findMergedAnnotation(method, MessageRequest.class);
        if (onMessage != null) {
            result.add(new HandlerMethodRegistry.HandlerMethodMappingInfo(
                    HandlerMethodRegistry.MappingType.MESSAGE,
                    "MessageRequest",
                    onMessage.textRegex(),
                    Pattern.compile(onMessage.textRegex()),
                    null,
                    onMessage.state(),
                    onMessage.order()));
        }

        CallbackRequest onCallback = AnnotatedElementUtils.findMergedAnnotation(method, CallbackRequest.class);
        if (onCallback != null) {
            result.add(new HandlerMethodRegistry.HandlerMethodMappingInfo(
                    HandlerMethodRegistry.MappingType.CALLBACK,
                    "CallbackRequest",
                    onCallback.prefix(),
                    null,
                    null,
                    onCallback.state(),
                    onCallback.order()));
        }

        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, BotStartedRequest.class),
                "BotStartedRequest", UpdateType.BOT_STARTED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, BotAddedRequest.class),
                "BotAddedRequest", UpdateType.BOT_ADDED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, BotRemovedRequest.class),
                "BotRemovedRequest", UpdateType.BOT_REMOVED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, MessageEditedRequest.class),
                "MessageEditedRequest", UpdateType.MESSAGE_EDITED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, MessageRemovedRequest.class),
                "MessageRemovedRequest", UpdateType.MESSAGE_REMOVED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, UserAddedRequest.class),
                "UserAddedRequest", UpdateType.USER_ADDED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, UserRemovedRequest.class),
                "UserRemovedRequest", UpdateType.USER_REMOVED);
        addTypeMapping(result, method, AnnotatedElementUtils.findMergedAnnotation(method, ChatTitleChangedRequest.class),
                "ChatTitleChangedRequest", UpdateType.CHAT_TITLE_CHANGED);

        return result;
    }

    private void addTypeMapping(List<HandlerMethodRegistry.HandlerMethodMappingInfo> result,
                                Method method,
                                @Nullable Object annotation,
                                String annotationType,
                                UpdateType updateType) {
        if (annotation == null) {
            return;
        }

        int order;
        if (annotation instanceof BotStartedRequest value) {
            order = value.order();
        } else if (annotation instanceof BotAddedRequest value) {
            order = value.order();
        } else if (annotation instanceof BotRemovedRequest value) {
            order = value.order();
        } else if (annotation instanceof MessageEditedRequest value) {
            order = value.order();
        } else if (annotation instanceof MessageRemovedRequest value) {
            order = value.order();
        } else if (annotation instanceof UserAddedRequest value) {
            order = value.order();
        } else if (annotation instanceof UserRemovedRequest value) {
            order = value.order();
        } else if (annotation instanceof ChatTitleChangedRequest value) {
            order = value.order();
        } else {
            throw new IllegalStateException("Unsupported annotation for " + method);
        }

        result.add(new HandlerMethodRegistry.HandlerMethodMappingInfo(
                HandlerMethodRegistry.MappingType.UPDATE_TYPE,
                annotationType,
                null,
                null,
                updateType,
                "",
                order));
    }

    private String normalizeCommand(String command) {
        String normalized = command.strip();
        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }

    @Override
    public void afterSingletonsInstantiated() {
        if (!(beanFactory instanceof ListableBeanFactory listableBeanFactory)) {
            return;
        }
        for (String beanName : listableBeanFactory.getBeanNamesForAnnotation(MaxController.class)) {
            listableBeanFactory.getBean(beanName);
        }
        nonAnnotatedClasses.clear();
    }
}

