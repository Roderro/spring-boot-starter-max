package ru.maxbot.core.dispatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.exception.HandlerExceptionResolver;
import ru.maxbot.core.handler.Handler;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.ChatMember;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.OutgoingMessage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateDispatcherTest {

    private final MaxApi noopApi = new NoopMaxApi();

    @Test
    void interceptorPreHandleCanRejectUpdate() {
        var handled = new AtomicBoolean(false);
        var handler = testHandler(ctx -> handled.set(true));

        var rejectingInterceptor = new HandlerInterceptor() {
            @Override
            public boolean preHandle(UpdateContext ctx) {
                return false;
            }
        };

        var dispatcher = new UpdateDispatcher(List.of(handler), List.of(rejectingInterceptor), null);
        dispatcher.dispatch(noopApi, Update.of(1L, "/test", null));

        assertFalse(handled.get());
    }

    @Test
    void interceptorPostHandleCalled() {
        var postHandled = new AtomicBoolean(false);
        var handler = testHandler(ctx -> {});

        var interceptor = new HandlerInterceptor() {
            @Override
            public void postHandle(UpdateContext ctx) {
                postHandled.set(true);
            }
        };

        var dispatcher = new UpdateDispatcher(List.of(handler), List.of(interceptor), null);
        dispatcher.dispatch(noopApi, Update.of(1L, "/test", null));

        assertTrue(postHandled.get());
    }

    @Test
    void exceptionResolverReceivesException() {
        var caughtEx = new AtomicReference<Exception>();
        var handler = testHandler(ctx -> {
            throw new RuntimeException("boom");
        });

        HandlerExceptionResolver exceptionResolver = (ctx, resolvedHandler, ex) -> {
            caughtEx.set(ex);
            return true;
        };

        var dispatcher = new UpdateDispatcher(List.of(handler), List.of(), List.of(exceptionResolver));
        dispatcher.dispatch(noopApi, Update.of(1L, "/test", null));

        assertTrue(caughtEx.get() instanceof RuntimeException);
        assertEquals("boom", caughtEx.get().getMessage());
    }

    @Test
    void interceptorAfterCompletionReceivesException() {
        var afterCompletionCalled = new AtomicBoolean(false);
        var handler = testHandler(ctx -> {
            throw new RuntimeException("fail");
        });

        var interceptor = new HandlerInterceptor() {
            @Override
            public void afterCompletion(UpdateContext ctx, Exception ex) {
                afterCompletionCalled.set(ex instanceof RuntimeException);
            }
        };

        var dispatcher = new UpdateDispatcher(List.of(handler), List.of(interceptor), null);
        dispatcher.dispatch(noopApi, Update.of(1L, "/test", null));

        assertTrue(afterCompletionCalled.get());
    }

    private Handler testHandler(Consumer<UpdateContext> action) {
        return new Handler() {
            @Override
            public boolean supports(Update update) {
                return "/test".equals(update.text());
            }

            @Override
            public void handle(UpdateContext ctx) {
                action.accept(ctx);
            }
        };
    }

    static class NoopMaxApi implements MaxApi {
        @Override public void sendMessage(long chatId, String text) {}
        @Override public void sendMessage(long chatId, OutgoingMessage message) {}
        @Override public void editMessage(String messageId, String text) {}
        @Override public void editMessage(String messageId, OutgoingMessage message) {}
        @Override public void deleteMessage(String messageId) {}
        @Override public void answerCallback(String callbackId, String notification) {}
        @Override public void answerCallback(String callbackId, OutgoingMessage message) {}
        @Override public User getMe() { return null; }
        @Override public String uploadImage(File file) { return null; }
        @Override public String uploadVideo(File file) { return null; }
        @Override public String uploadAudio(File file) { return null; }
        @Override public String uploadFile(File file) { return null; }
        @Override public Chat getChat(long chatId) { return null; }
        @Override public List<ChatMember> getChatMembers(long chatId) { return List.of(); }
        @Override public void leaveChat(long chatId) {}
    }
}


