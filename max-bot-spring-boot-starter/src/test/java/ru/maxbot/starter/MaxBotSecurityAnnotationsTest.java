package ru.maxbot.starter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
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
import ru.maxbot.starter.annotations.CommandRequest;
import ru.maxbot.starter.annotations.MaxBotControllerAdvice;
import ru.maxbot.starter.annotations.MaxBotExceptionHandler;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.security.BotAuthenticationConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MaxBotSecurityAnnotationsTest {

    @Test
    void preAuthorizeUsesSpringSecurityContext() {
        var api = new CapturingMaxApi();

        try (var ctx = new AnnotationConfigApplicationContext()) {
            ctx.registerBean(MaxApi.class, () -> api);
            ctx.registerBean(BotAuthenticationConverter.class, TestBotAuthenticationConverter::new);
            ctx.register(MaxBotSecurityAutoConfiguration.class);
            ctx.register(MaxBotAnnotationAutoConfiguration.class);
            ctx.register(MaxBotDispatchAutoConfiguration.class);
            ctx.register(SecureController.class);
            ctx.register(SecurityAdvice.class);
            ctx.refresh();

            var dispatcher = ctx.getBean(UpdateDispatcher.class);
            var admin = new User(1L, "Admin", null, "admin", false, null);
            var user = new User(2L, "User", null, "user", false, null);

            dispatcher.dispatch(api, Update.ofMessage(1L, null, "/admin", admin, System.currentTimeMillis(), "ru"));
            assertEquals(List.of("admin:1"), api.messages);

            api.messages.clear();

            dispatcher.dispatch(api, Update.ofMessage(1L, null, "/admin", user, System.currentTimeMillis(), "ru"));
            assertEquals(List.of("denied"), api.messages);
        }
    }

    @MaxController
    static class SecureController {
        @PreAuthorize("hasRole('ADMIN')")
        @CommandRequest("admin")
        public void admin(UpdateContext ctx, Authentication authentication) {
            User principal = (User) authentication.getPrincipal();
            ctx.reply("admin:" + principal.getUserId());
        }
    }

    @MaxBotControllerAdvice
    static class SecurityAdvice {

        @MaxBotExceptionHandler(AuthorizationDeniedException.class)
        public void handleDenied(UpdateContext ctx, AuthorizationDeniedException ex) {
            ctx.reply("denied");
        }
    }

    static class TestBotAuthenticationConverter implements BotAuthenticationConverter {
        private static final Authentication ANONYMOUS = new AnonymousAuthenticationToken(
                "maxbot-anonymous-key",
                "anonymousUser",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")
        );

        @Override
        public Authentication convert(UpdateContext context) {
            User sender = context.sender();
            if (sender == null) {
                return ANONYMOUS;
            }
            List<String> authorities = new ArrayList<>();
            authorities.add("ROLE_USER");
            if (Long.valueOf(1L).equals(sender.getUserId())) {
                authorities.add("ROLE_ADMIN");
            }
            return UsernamePasswordAuthenticationToken.authenticated(
                    sender,
                    "N/A",
                    AuthorityUtils.createAuthorityList(authorities)
            );
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

