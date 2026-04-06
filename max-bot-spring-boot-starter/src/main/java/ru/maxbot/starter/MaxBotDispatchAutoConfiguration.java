package ru.maxbot.starter;

import java.util.List;

import ru.maxbot.core.dispatcher.UpdateDispatcher;
import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.handler.Handler;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.core.state.InMemoryStateStore;
import ru.maxbot.core.state.StateStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@AutoConfigureAfter(MaxBotAnnotationAutoConfiguration.class)
public class MaxBotDispatchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public StateStore stateStore() {
        return new InMemoryStateStore();
    }

    @Bean
    @Lazy
    @ConditionalOnMissingBean
    public UpdateDispatcher updateDispatcher(
            ObjectProvider<Handler> handlersProvider,
            ObjectProvider<HandlerInterceptor> interceptorsProvider,
            ObjectProvider<HandlerExceptionResolver> exceptionResolversProvider,
            ObjectProvider<StateStore> stateStoreProvider) {
        List<HandlerInterceptor> interceptors = interceptorsProvider.orderedStream().toList();
        List<HandlerExceptionResolver> exceptionResolvers = exceptionResolversProvider.orderedStream().toList();
        StateStore stateStore = stateStoreProvider.getIfAvailable();
        return new UpdateDispatcher(() -> handlersProvider.orderedStream().toList(),
                interceptors, exceptionResolvers, stateStore);
    }
}


