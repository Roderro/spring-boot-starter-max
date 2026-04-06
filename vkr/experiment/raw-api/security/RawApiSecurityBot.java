package ru.maxbot.experiment.rawapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RawApiSecurityBot {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String token;
    private final AccessPolicy accessPolicy = new AccessPolicy();

    public RawApiSecurityBot(String token) {
        this.token = token;
    }

    public void handle(IncomingUpdate update) throws Exception {
        if (update.message == null || update.message.body == null || update.message.sender == null) {
            return;
        }

        String text = update.message.body.text;
        long chatId = update.message.recipient.chatId;
        String username = update.message.sender.username;
        long userId = update.message.sender.userId;

        if ("/whoami".equals(text)) {
            sendMessage(chatId, "Вы вошли как @" + username + " (userId=" + userId + ")");
            return;
        }

        if ("/admin".equals(text)) {
            if (!accessPolicy.hasRole(username, userId, "ADMIN")) {
                sendMessage(chatId, "Доступ запрещен. Для команды /admin требуется роль ADMIN.");
                return;
            }
            sendMessage(chatId, "Команда /admin доступна. Проверка роли пройдена.");
        }
    }

    private void sendMessage(long chatId, String text) throws Exception {
        String body = mapper.writeValueAsString(Map.of(
                "chat_id", chatId,
                "text", text
        ));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://platform-api.max.ru/messages"))
                .header("Authorization", token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) {
            throw new IllegalStateException("MAX API error: " + response.statusCode());
        }
    }

    static class AccessPolicy {
        boolean hasRole(String username, long userId, String role) {
            List<String> roles = resolveRoles(username, userId);
            return roles.contains(role);
        }

        private List<String> resolveRoles(String username, long userId) {
            if ("admin".equalsIgnoreCase(username) || userId == 1L) {
                return List.of("USER", "ADMIN");
            }
            return List.of("USER");
        }
    }

    static class IncomingUpdate {
        public IncomingMessage message;
    }

    static class IncomingMessage {
        public IncomingBody body;
        public IncomingRecipient recipient;
        public IncomingUser sender;
    }

    static class IncomingBody {
        public String text;
    }

    static class IncomingRecipient {
        public long chatId;
    }

    static class IncomingUser {
        public long userId;
        public String username;
    }
}

