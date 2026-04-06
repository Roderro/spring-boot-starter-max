package ru.maxbot.starter.internal.registry;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.MethodIntrospector;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotatedElementUtils;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;

final class ExceptionHandlerMethodIntrospector {

    private ExceptionHandlerMethodIntrospector() {
    }

    static Map<Method, List<Class<? extends Throwable>>> findExceptionHandlerMethods(Class<?> targetClass) {
        return MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<List<Class<? extends Throwable>>>) method -> {
                    List<Class<? extends Throwable>> exceptionTypes = resolveExceptionTypes(method);
                    return exceptionTypes.isEmpty() ? null : exceptionTypes;
                });
    }

    static int getOrder(Object bean, Class<?> targetClass) {
        if (bean instanceof Ordered ordered) {
            return ordered.getOrder();
        }
        org.springframework.core.annotation.Order order =
                AnnotatedElementUtils.findMergedAnnotation(targetClass, org.springframework.core.annotation.Order.class);
        return order != null ? order.value() : Ordered.LOWEST_PRECEDENCE;
    }

    private static List<Class<? extends Throwable>> resolveExceptionTypes(Method method) {
        MaxBotExceptionHandler exceptionHandler =
                AnnotatedElementUtils.findMergedAnnotation(method, MaxBotExceptionHandler.class);
        if (exceptionHandler == null) {
            return List.of();
        }

        List<Class<? extends Throwable>> exceptionTypes = new ArrayList<>(List.of(exceptionHandler.value()));
        if (exceptionTypes.isEmpty()) {
            for (Class<?> parameterType : method.getParameterTypes()) {
                if (Throwable.class.isAssignableFrom(parameterType)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) parameterType;
                    exceptionTypes.add(exceptionType);
                }
            }
        }

        if (exceptionTypes.isEmpty()) {
            throw new IllegalStateException("Invalid exception handler method " + method +
                    ": no exception types declared");
        }

        return List.copyOf(exceptionTypes);
    }
}

