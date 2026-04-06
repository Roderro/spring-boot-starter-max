package ru.maxbot.starter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.Order;
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
import ru.maxbot.starter.annotations.BotStartedRequest;
import ru.maxbot.starter.annotations.CallbackRequest;
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxBotAnnotationHandlersTest {

    @Test
    void annotationBasedHandlersAreRegisteredAndDispatched() {
        var capturingApi = new CapturingMaxApi();

        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(MaxApi.class, () -> capturingApi);
            ctx.register(MaxBotAnnotationAutoConfiguration.class);
            ctx.register(MaxBotDispatchAutoConfiguration.class);
            ctx.register(TestBot.class);
            ctx.register(GlobalBotAdvice.class);
            ctx.refresh();

            var dispatcher = ctx.getBean(UpdateDispatcher.class);

            dispatcher.dispatch(capturingApi, Update.of(1L, "/start", null));
            assertEquals(List.of("Hello!"), capturingApi.messages);

            capturingApi.messages.clear();

            dispatcher.dispatch(capturingApi, Update.of(1L, "order", null));
            assertEquals(List.of("Order received"), capturingApi.messages);

            capturingApi.messages.clear();

            dispatcher.dispatch(capturingApi, Update.of(1L, null, "pay:123"));
            assertEquals(List.of("Payment: pay:123"), capturingApi.messages);

            capturingApi.messages.clear();

            var user = new User(42L, "User", null, "user42", false, null);
            dispatcher.dispatch(capturingApi, Update.ofBotStarted(1L, user, System.currentTimeMillis(), null, "ru"));
            assertEquals(List.of("Welcome, User!"), capturingApi.messages);

            capturingApi.messages.clear();

            dispatcher.dispatch(capturingApi, Update.of(1L, "/failLocal", null));
            assertEquals(List.of("Local: local failure"), capturingApi.messages);

            capturingApi.messages.clear();

            dispatcher.dispatch(capturingApi, Update.of(1L, "/failGlobal", null));
            assertEquals(List.of("Global: global failure"), capturingApi.messages);
        }
    }

    @MaxController
    static class TestBot {

        @CommandRequest("start")
        public void start(MaxApi api, Update update) {
            api.sendMessage(update.chatId(), "Hello!");
        }

        @MessageRequest(textRegex = "(?i)(заказ|order)")
        public void order(UpdateContext ctx, User user) {
            ctx.reply(user == null ? "Order received" : "Order received");
        }

        @CallbackRequest(prefix = "pay:")
        public void pay(Update update, UpdateContext ctx) {
            ctx.reply("Payment: " + update.callbackData());
        }

        @BotStartedRequest
        public void botStarted(User user, UpdateContext ctx) {
            ctx.reply("Welcome, " + user.getFirstName() + "!");
        }

        @CommandRequest("failLocal")
        public void failLocal() {
            throw new IllegalStateException("local failure");
        }

        @CommandRequest("failGlobal")
        public void failGlobal() {
            throw new IllegalArgumentException("global failure");
        }

        @MaxBotExceptionHandler
        public void handleLocal(IllegalStateException ex, UpdateContext ctx) {
            ctx.reply("Local: " + ex.getMessage());
        }
    }

    @MaxBotControllerAdvice
    @Order(10)
    static class GlobalBotAdvice {

        @MaxBotExceptionHandler(IllegalArgumentException.class)
        public void handleGlobal(UpdateContext ctx, IllegalArgumentException ex) {
            ctx.reply("Global: " + ex.getMessage());
        }
    }

    static class CapturingMaxApi implements MaxApi {
        final List<String> messages = new ArrayList<>();

        @Override public void sendMessage(long chatId, String text) { messages.add(text); }
        @Override public void sendMessage(long chatId, OutgoingMessage message) { messages.add(message.text()); }
        @Override public void editMessage(String messageId, String text) {}
        @Override public void editMessage(String messageId, OutgoingMessage message) {}
        @Override public void deleteMessage(String messageId) {}
        @Override public void answerCallback(String callbackId, String notification) {}
        @Override public void answerCallback(String callbackId, OutgoingMessage message) {}
        @Override public User getMe() { return new User(0L, "TestBot", null, "test_bot", true, null); }
        @Override public String uploadImage(File file) { return "img-token"; }
        @Override public String uploadVideo(File file) { return "vid-token"; }
        @Override public String uploadAudio(File file) { return "aud-token"; }
        @Override public String uploadFile(File file) { return "file-token"; }
        @Override public Chat getChat(long chatId) { return new Chat(chatId, ChatType.CHAT, ChatStatus.ACTIVE, "Test", null, null, 2, false, null); }
        @Override public List<ChatMember> getChatMembers(long chatId) { return List.of(); }
        @Override public void leaveChat(long chatId) {}
    }
}

