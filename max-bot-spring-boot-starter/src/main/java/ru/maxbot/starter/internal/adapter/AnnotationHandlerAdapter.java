package ru.maxbot.starter.internal.adapter;

import ru.maxbot.starter.internal.registry.HandlerMethodRegistry;
import ru.maxbot.starter.internal.invocation.InvocableBotHandlerMethod;
import ru.maxbot.starter.internal.argument.HandlerMethodArgumentResolverComposite;
import ru.maxbot.starter.internal.security.HandlerMethodAuthorizationEvaluator;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.handler.Handler;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.state.StateStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.Ordered;

public class AnnotationHandlerAdapter implements Handler, Ordered {

    private final HandlerMethodRegistry handlerMethodContainer;
    private final HandlerMethodArgumentResolverComposite argumentResolvers;
    private final HandlerMethodAuthorizationEvaluator authorizationEvaluator;
    private final ObjectProvider<StateStore> stateStoreProvider;

    public AnnotationHandlerAdapter(HandlerMethodRegistry handlerMethodContainer,
                                    HandlerMethodArgumentResolverComposite argumentResolvers,
                                    HandlerMethodAuthorizationEvaluator authorizationEvaluator,
                                    ObjectProvider<StateStore> stateStoreProvider) {
        this.handlerMethodContainer = handlerMethodContainer;
        this.argumentResolvers = argumentResolvers;
        this.authorizationEvaluator = authorizationEvaluator;
        this.stateStoreProvider = stateStoreProvider;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public boolean supports(Update update) {
        return handlerMethodContainer.hasMatchingHandler(update, stateStoreProvider.getIfAvailable());
    }

    @Override
    public void handle(UpdateContext ctx) {
        HandlerMethodRegistry.HandlerLookupResult lookupResult = handlerMethodContainer.lookupHandlerMethod(ctx);
        if (lookupResult.handlerMethod() == null) {
            return;
        }
        new InvocableBotHandlerMethod(lookupResult.handlerMethod(), argumentResolvers, authorizationEvaluator).invoke(ctx);
    }
}


