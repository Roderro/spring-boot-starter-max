package ru.maxbot.core.dispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.exception.HandlerExceptionResolverComposite;
import ru.maxbot.core.handler.Handler;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.state.StateStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UpdateDispatcher {

    private static final Logger log = LoggerFactory.getLogger(UpdateDispatcher.class);

    private final Supplier<List<Handler>> handlersSupplier;
    private final List<HandlerInterceptor> interceptors;
    private final HandlerExceptionResolverComposite exceptionResolvers;
    private final StateStore stateStore;

    public UpdateDispatcher(List<Handler> handlers,
                            List<HandlerInterceptor> interceptors,
                            List<HandlerExceptionResolver> exceptionResolvers) {
        this(() -> handlers, interceptors, exceptionResolvers, null);
    }

    public UpdateDispatcher(Supplier<List<Handler>> handlersSupplier,
                            List<HandlerInterceptor> interceptors,
                            List<HandlerExceptionResolver> exceptionResolvers,
                            StateStore stateStore) {
        if (handlersSupplier == null) {
            throw new IllegalArgumentException("handlersSupplier must not be null");
        }
        this.handlersSupplier = handlersSupplier;
        this.interceptors = interceptors != null ? List.copyOf(interceptors) : List.of();
        this.exceptionResolvers = new HandlerExceptionResolverComposite(exceptionResolvers);
        this.stateStore = stateStore;
    }

    public void dispatch(MaxApi api, Update update) {
        UpdateContext ctx = new UpdateContext(api, update, stateStore);
        List<HandlerInterceptor> appliedInterceptors = new ArrayList<>(interceptors.size());

        for (HandlerInterceptor interceptor : interceptors) {
            try {
                if (!interceptor.preHandle(ctx)) {
                    log.debug("Interceptor {} rejected update type={} chatId={}",
                            interceptor.getClass().getSimpleName(), update.type(), update.chatId());
                    triggerAfterCompletion(appliedInterceptors, ctx, null);
                    return;
                }
                appliedInterceptors.add(interceptor);
            } catch (Exception e) {
                processHandlerException(ctx, null, e);
                triggerAfterCompletion(appliedInterceptors, ctx, e);
                return;
            }
        }

        for (Handler handler : handlers()) {
            if (!handler.supports(update)) {
                continue;
            }

            log.debug("Dispatching update type={} chatId={} to {}",
                    update.type(), update.chatId(), handler.getClass().getSimpleName());

            Exception dispatchException = null;
            try {
                handler.handle(ctx);
                triggerPostHandle(appliedInterceptors, ctx);
            } catch (Exception e) {
                dispatchException = e;
                processHandlerException(ctx, handler, e);
            } finally {
                triggerAfterCompletion(appliedInterceptors, ctx, dispatchException);
            }
            return;
        }

        log.debug("No handler found for update type={} chatId={}", update.type(), update.chatId());
        triggerAfterCompletion(appliedInterceptors, ctx, null);
    }

    private List<Handler> handlers() {
        List<Handler> handlers = handlersSupplier.get();
        if (handlers == null) {
            throw new IllegalStateException("handlersSupplier returned null");
        }
        return handlers;
    }

    private void triggerPostHandle(List<HandlerInterceptor> appliedInterceptors, UpdateContext ctx) throws Exception {
        for (int i = appliedInterceptors.size() - 1; i >= 0; i--) {
            appliedInterceptors.get(i).postHandle(ctx);
        }
    }

    private void triggerAfterCompletion(List<HandlerInterceptor> appliedInterceptors, UpdateContext ctx, Exception ex) {
        for (int i = appliedInterceptors.size() - 1; i >= 0; i--) {
            try {
                appliedInterceptors.get(i).afterCompletion(ctx, ex);
            } catch (Exception e) {
                log.error("Interceptor afterCompletion failed", e);
            }
        }
    }

    private void processHandlerException(UpdateContext ctx, Handler handler, Exception ex) {
        if (!exceptionResolvers.resolveException(ctx, handler, ex)) {
            String handlerName = handler != null ? handler.getClass().getSimpleName() : "<interceptor>";
            log.error("Handler {} threw an exception for update type={} chatId={}",
                    handlerName, ctx.update().type(), ctx.update().chatId(), ex);
        }
    }
}

