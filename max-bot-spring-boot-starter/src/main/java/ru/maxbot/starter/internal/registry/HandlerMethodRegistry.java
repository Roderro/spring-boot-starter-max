package ru.maxbot.starter.internal.registry;

import ru.maxbot.starter.internal.invocation.BotHandlerMethod;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.model.UpdateType;
import ru.maxbot.core.state.StateStore;

public class HandlerMethodRegistry {

    private final List<RequestMapping> mappings = new ArrayList<>();

    public synchronized BotHandlerMethod registerController(Object bean, Method method,
                                                               List<HandlerMethodMappingInfo> mappingInfos) {
        if (mappingInfos.isEmpty()) {
            return null;
        }

        BotHandlerMethod handlerMethod = new BotHandlerMethod(bean, method);
        for (HandlerMethodMappingInfo mappingInfo : mappingInfos) {
            mappings.add(new RequestMapping(mappingInfo, handlerMethod));
        }
        mappings.sort(Comparator.comparingInt((RequestMapping mapping) -> mapping.mappingInfo().order())
                .thenComparing(mapping -> mapping.handlerMethod().getBeanType().getName())
                .thenComparing(mapping -> mapping.handlerMethod().getMethod().getName()));
        return handlerMethod;
    }

    public synchronized HandlerLookupResult lookupHandlerMethod(UpdateContext ctx) {
        Update update = ctx.update();
        for (RequestMapping mapping : mappings) {
            if (mapping.mappingInfo().matches(update, ctx.state())) {
                return new HandlerLookupResult(mapping.handlerMethod(), mapping.mappingInfo());
            }
        }
        return new HandlerLookupResult(null, null);
    }

    public synchronized boolean hasMatchingHandler(Update update, StateStore stateStore) {
        String state = stateStore != null ? stateStore.getState(update.chatId()) : null;
        for (RequestMapping mapping : mappings) {
            if (mapping.mappingInfo().matches(update, state)) {
                return true;
            }
        }
        return false;
    }

    public record HandlerLookupResult(BotHandlerMethod handlerMethod, HandlerMethodMappingInfo mappingInfo) {
    }

    public record RequestMapping(HandlerMethodMappingInfo mappingInfo, BotHandlerMethod handlerMethod) {
    }

    public enum MappingType {
        COMMAND,
        MESSAGE,
        CALLBACK,
        UPDATE_TYPE
    }

    public record HandlerMethodMappingInfo(
            MappingType mappingType,
            String annotationType,
            String rawValue,
            Pattern regex,
            UpdateType updateType,
            String state,
            int order
    ) {
        public boolean matches(Update update, String currentState) {
            if (state != null && !state.isEmpty() && !state.equals(currentState)) {
                return false;
            }

            return switch (mappingType) {
                case COMMAND -> matchesCommand(update);
                case MESSAGE -> matchesMessage(update);
                case CALLBACK -> matchesCallback(update);
                case UPDATE_TYPE -> update.type() == updateType;
            };
        }

        private boolean matchesCommand(Update update) {
            if (update.text() == null) {
                return false;
            }
            String text = update.text().trim();
            String slash = "/" + rawValue;
            return text.equals(slash) || text.startsWith(slash + " ");
        }

        private boolean matchesMessage(Update update) {
            return update.text() != null && regex != null && regex.matcher(update.text()).matches();
        }

        private boolean matchesCallback(Update update) {
            return update.callbackData() != null && rawValue != null && update.callbackData().startsWith(rawValue);
        }
    }
}


