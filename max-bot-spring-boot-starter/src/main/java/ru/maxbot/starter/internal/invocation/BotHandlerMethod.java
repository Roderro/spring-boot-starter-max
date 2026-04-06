package ru.maxbot.starter.internal.invocation;

import java.lang.reflect.Method;

import org.springframework.core.BridgeMethodResolver;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.SynthesizingMethodParameter;
import org.springframework.util.ClassUtils;

public class BotHandlerMethod {

    private final Object bean;
    private final Class<?> beanType;
    private final Method method;
    private final Method bridgedMethod;
    private final MethodParameter[] methodParameters;

    public BotHandlerMethod(Object bean, Method method) {
        this.bean = bean;
        this.beanType = ClassUtils.getUserClass(bean);
        this.method = method;
        this.bridgedMethod = BridgeMethodResolver.findBridgedMethod(method);
        this.methodParameters = initMethodParameters();
    }

    public Object getBean() {
        return bean;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public Method getMethod() {
        return method;
    }

    public Method getBridgedMethod() {
        return bridgedMethod;
    }

    public MethodParameter[] getMethodParameters() {
        return methodParameters;
    }

    private MethodParameter[] initMethodParameters() {
        int count = bridgedMethod.getParameterCount();
        MethodParameter[] result = new MethodParameter[count];
        for (int i = 0; i < count; i++) {
            MethodParameter parameter = new SynthesizingMethodParameter(bridgedMethod, i);
            GenericTypeResolver.resolveParameterType(parameter, beanType);
            result[i] = parameter;
        }
        return result;
    }
}


