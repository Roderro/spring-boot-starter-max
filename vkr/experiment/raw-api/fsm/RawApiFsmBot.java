package ru.maxbot.experiment.rawapi.fsm;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RawApiFsmBot {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Map<Long, String> states = new ConcurrentHashMap<>();
    private final String token;

    public RawApiFsmBot(String token) {
        this.token = token;
    }

    public void handle(IncomingUpdate update) throws Exception {
        if (update.message == null || update.message.body == null || update.message.recipient == null) {
            return;
        }

        long chatId = update.message.recipient.chatId;
        String text = update.message.body.text;
        String state = states.get(chatId);

        if ("/cancel".equals(text)) {
            states.remove(chatId);
            sendMessage(chatId, "Диалог отменен.");
            return;
        }

        if ("/survey".equals(text)) {
            states.put(chatId, "WAIT_NAME");
            sendMessage(chatId, "Как вас зовут?");
            return;
        }

        if ("WAIT_NAME".equals(state) && text != null && text.trim().length() >= 2) {
            states.put(chatId, "WAIT_COUNT");
            sendMessage(chatId, "Приятно познакомиться, " + text + ". Сколько уведомлений отправить?");
            return;
        }

        if ("WAIT_COUNT".equals(state) && text != null && text.matches("\\d{1,2}")) {
            states.remove(chatId);
            sendMessage(chatId, "Сценарий завершен. Будет отправлено уведомлений: " + text);
            return;
        }

        if ("WAIT_COUNT".equals(state)) {
            sendMessage(chatId, "Введите целое число от 0 до 99.");
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

    static class IncomingUpdate {
        public IncomingMessage message;
    }

    static class IncomingMessage {
        public IncomingBody body;
        public IncomingRecipient recipient;
    }

    static class IncomingBody {
        public String text;
    }

    static class IncomingRecipient {
        public long chatId;
    }
}

