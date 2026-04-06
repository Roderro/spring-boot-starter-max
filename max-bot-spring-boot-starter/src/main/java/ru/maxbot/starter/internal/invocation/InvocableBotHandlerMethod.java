package ru.maxbot.starter.internal.invocation;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolver;
import ru.maxbot.starter.internal.security.HandlerMethodAuthorizationEvaluator;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

public class InvocableBotHandlerMethod extends BotHandlerMethod {

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final HandlerMethodArgumentResolver argumentResolver;
    private final HandlerMethodAuthorizationEvaluator authorizationEvaluator;

    public InvocableBotHandlerMethod(BotHandlerMethod handlerMethod,
                                     HandlerMethodArgumentResolver argumentResolver,
                                     HandlerMethodAuthorizationEvaluator authorizationEvaluator) {
        super(handlerMethod.getBean(), handlerMethod.getMethod());
        this.argumentResolver = argumentResolver;
        this.authorizationEvaluator = authorizationEvaluator;
    }

    public Object invoke(UpdateContext ctx) {
        return invoke(new HandlerMethodInvocationContext(ctx, null));
    }

    public Object invokeExceptionHandler(UpdateContext ctx, Exception exception) {
        return invoke(new HandlerMethodInvocationContext(ctx, exception));
    }

    private Object invoke(HandlerMethodInvocationContext context) {
        Object[] args = getMethodArgumentValues(context);
        try {
            authorizationEvaluator.authorizePre(this, args);
            ReflectionUtils.makeAccessible(getBridgedMethod());
            return getBridgedMethod().invoke(getBean(), args);
        } catch (InvocationTargetException ex) {
            Throwable targetException = ex.getTargetException();
            if (targetException instanceof Exception exception) {
                throw new BotHandlerMethodInvocationException(this,
                        getInvocationErrorMessage("Failed to invoke handler method", args), exception);
            }
            if (targetException instanceof Error error) {
                throw error;
            }
            throw new IllegalStateException(getInvocationErrorMessage("Failed to invoke handler method", args),
                    targetException);
        } catch (Exception ex) {
            throw new BotHandlerMethodInvocationException(this,
                    getInvocationErrorMessage("Failed to invoke handler method", args), ex);
        }
    }

    private Object[] getMethodArgumentValues(HandlerMethodInvocationContext context) {
        MethodParameter[] parameters = getMethodParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            parameter.initParameterNameDiscovery(parameterNameDiscoverer);
            args[i] = argumentResolver.resolveArgument(parameter, context);
        }
        return args;
    }

    private String getInvocationErrorMessage(String text, Object[] resolvedArgs) {
        return text + "\nHandlerMethod details:\n" +
                "Controller [" + getBeanType().getName() + "]\n" +
                "Method [" + ClassUtils.getQualifiedMethodName(getMethod(), getBeanType()) + "]\n" +
                "Resolved arguments " + Arrays.toString(resolvedArgs);
    }
}


