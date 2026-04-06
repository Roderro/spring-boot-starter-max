package ru.maxbot.starter.internal.security;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInvocation;
import ru.maxbot.starter.internal.invocation.BotHandlerMethod;

final class BotMethodInvocation implements MethodInvocation {

    private final BotHandlerMethod handlerMethod;
    private final Object[] arguments;

    BotMethodInvocation(BotHandlerMethod handlerMethod, Object[] arguments) {
        this.handlerMethod = handlerMethod;
        this.arguments = arguments;
    }

    @Override
    public Method getMethod() {
        return handlerMethod.getBridgedMethod();
    }

    @Override
    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public Object proceed() {
        throw new UnsupportedOperationException("BotMethodInvocation is evaluation-only");
    }

    @Override
    public Object getThis() {
        return handlerMethod.getBean();
    }

    @Override
    public AccessibleObject getStaticPart() {
        return handlerMethod.getBridgedMethod();
    }
}

