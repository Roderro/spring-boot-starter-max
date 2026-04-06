package ru.maxbot.starter;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.dispatcher.UpdateDispatcher;
import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.ChatMember;
import ru.maxbot.core.model.ChatStatus;
import ru.maxbot.core.model.ChatType;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.OutgoingMessage;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MessageRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxBotMessageRegexMatchingTest {

    @Test
    void onMessageUsesWholeStringMatch() {
        RegexBot.lastHandled.set(null);
        var api = new NoopMaxApi();

        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(MaxApi.class, () -> api);
            ctx.register(MaxBotAnnotationAutoConfiguration.class);
            ctx.register(MaxBotDispatchAutoConfiguration.class);
            ctx.register(RegexBot.class);
            ctx.refresh();

            var dispatcher = ctx.getBean(UpdateDispatcher.class);
            dispatcher.dispatch(api, Update.of(1L, "/order", null));
            dispatcher.dispatch(api, Update.of(1L, "Lenina street 15", null));

            assertEquals("LONG", RegexBot.lastHandled.get());
        }
    }

    @MaxController
    static class RegexBot {
        static final AtomicReference<String> lastHandled = new AtomicReference<>();

        @CommandRequest("order")
        public void start(UpdateContext ctx) {
            ctx.setState("ORDER_ADDRESS");
        }

        @MessageRequest(textRegex = ".{5,}", state = "ORDER_ADDRESS")
        public void longAddress(UpdateContext ctx) {
            lastHandled.set("LONG");
        }

        @MessageRequest(textRegex = ".{0,4}", state = "ORDER_ADDRESS")
        public void shortAddress(UpdateContext ctx) {
            lastHandled.set("SHORT");
        }
    }

    static class NoopMaxApi implements MaxApi {
        @Override public void sendMessage(long chatId, String text) {}
        @Override public void sendMessage(long chatId, OutgoingMessage message) {}
        @Override public void editMessage(String messageId, String text) {}
        @Override public void editMessage(String messageId, OutgoingMessage message) {}
        @Override public void deleteMessage(String messageId) {}
        @Override public void answerCallback(String callbackId, String notification) {}
        @Override public void answerCallback(String callbackId, OutgoingMessage message) {}
        @Override public User getMe() { return new User(0L, "TestBot", null, "test_bot", true, null); }
        @Override public String uploadImage(File file) { return null; }
        @Override public String uploadVideo(File file) { return null; }
        @Override public String uploadAudio(File file) { return null; }
        @Override public String uploadFile(File file) { return null; }
        @Override public Chat getChat(long chatId) { return new Chat(chatId, ChatType.CHAT, ChatStatus.ACTIVE, "Test", null, null, 2, false, null); }
        @Override public List<ChatMember> getChatMembers(long chatId) { return List.of(); }
        @Override public void leaveChat(long chatId) {}
    }
}


