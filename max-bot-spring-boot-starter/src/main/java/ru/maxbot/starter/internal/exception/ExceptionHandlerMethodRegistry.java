package ru.maxbot.starter.internal.exception;

import ru.maxbot.starter.internal.invocation.BotHandlerMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExceptionHandlerMethodRegistry {

    private final Map<Class<?>, List<ExceptionHandlerMethodMapping>> controllerMappings = new ConcurrentHashMap<>();
    private final Map<Class<?>, ControllerAdviceExceptionHandlers> adviceMappingsByType = new ConcurrentHashMap<>();
    private final List<ControllerAdviceExceptionHandlers> adviceMappings = new ArrayList<>();

    public synchronized BotHandlerMethod registerControllerExceptionHandler(
            Object bean, Method method, List<Class<? extends Throwable>> exceptionTypes) {
        BotHandlerMethod handlerMethod = new BotHandlerMethod(bean, method);
        List<ExceptionHandlerMethodMapping> mappings = controllerMappings.computeIfAbsent(
                handlerMethod.getBeanType(), key -> new ArrayList<>());
        mappings.add(new ExceptionHandlerMethodMapping(handlerMethod, List.copyOf(exceptionTypes)));
        mappings.sort(Comparator.comparing(mapping -> mapping.handlerMethod().getMethod().getName()));
        return handlerMethod;
    }

    public synchronized BotHandlerMethod registerControllerAdviceExceptionHandler(
            Object bean, Method method, List<Class<? extends Throwable>> exceptionTypes, int order) {
        BotHandlerMethod handlerMethod = new BotHandlerMethod(bean, method);
        ControllerAdviceExceptionHandlers adviceHandlers = adviceMappingsByType.computeIfAbsent(
                handlerMethod.getBeanType(),
                key -> {
                    ControllerAdviceExceptionHandlers value =
                            new ControllerAdviceExceptionHandlers(order, handlerMethod.getBeanType(), new ArrayList<>());
                    adviceMappings.add(value);
                    adviceMappings.sort(Comparator.comparingInt(ControllerAdviceExceptionHandlers::order)
                            .thenComparing(mapping -> mapping.beanType().getName()));
                    return value;
                });
        adviceHandlers.mappings().add(new ExceptionHandlerMethodMapping(handlerMethod, List.copyOf(exceptionTypes)));
        adviceHandlers.mappings().sort(Comparator.comparing(mapping -> mapping.handlerMethod().getMethod().getName()));
        return handlerMethod;
    }

    public synchronized BotHandlerMethod lookupExceptionHandler(BotHandlerMethod handlerMethod, Exception exception) {
        BotHandlerMethod localHandler = findBestMatch(controllerMappings.get(handlerMethod.getBeanType()), exception);
        if (localHandler != null) {
            return localHandler;
        }

        for (ControllerAdviceExceptionHandlers advice : adviceMappings) {
            BotHandlerMethod adviceHandler = findBestMatch(advice.mappings(), exception);
            if (adviceHandler != null) {
                return adviceHandler;
            }
        }
        return null;
    }

    private BotHandlerMethod findBestMatch(List<ExceptionHandlerMethodMapping> mappings, Exception exception) {
        if (mappings == null || mappings.isEmpty()) {
            return null;
        }

        ExceptionHandlerMethodMapping bestMatch = null;
        int bestDepth = Integer.MAX_VALUE;
        for (ExceptionHandlerMethodMapping mapping : mappings) {
            int depth = getBestDepth(mapping.exceptionTypes(), exception.getClass());
            if (depth < bestDepth) {
                bestMatch = mapping;
                bestDepth = depth;
            }
        }
        return bestMatch != null ? bestMatch.handlerMethod() : null;
    }

    private int getBestDepth(List<Class<? extends Throwable>> declaredTypes, Class<?> actualType) {
        int bestDepth = Integer.MAX_VALUE;
        for (Class<? extends Throwable> declaredType : declaredTypes) {
            int depth = getDepth(declaredType, actualType);
            if (depth < bestDepth) {
                bestDepth = depth;
            }
        }
        return bestDepth;
    }

    private int getDepth(Class<? extends Throwable> declaredType, Class<?> actualType) {
        int depth = 0;
        Class<?> currentType = actualType;
        while (currentType != null) {
            if (declaredType.equals(currentType)) {
                return depth;
            }
            currentType = currentType.getSuperclass();
            depth++;
        }
        return Integer.MAX_VALUE;
    }

    private record ExceptionHandlerMethodMapping(BotHandlerMethod handlerMethod,
                                                 List<Class<? extends Throwable>> exceptionTypes) {
    }

    private record ControllerAdviceExceptionHandlers(int order,
                                                     Class<?> beanType,
                                                     List<ExceptionHandlerMethodMapping> mappings) {
    }
}

