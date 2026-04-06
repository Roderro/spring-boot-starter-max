package ru.maxbot.experiment.rawapi.minimal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RawApiMinimalBot {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String token;

    public RawApiMinimalBot(String token) {
        this.token = token;
    }

    public static void main(String[] args) throws Exception {
        String token = System.getenv("MAX_BOT_TOKEN");
        RawApiMinimalBot bot = new RawApiMinimalBot(token);
        bot.startWebhookServer(8080, "/webhook");
    }

    public void startWebhookServer(int port, String path) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, this::handleWebhook);
        server.start();
    }

    private void handleWebhook(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        IncomingUpdate update = mapper.readValue(exchange.getRequestBody(), IncomingUpdate.class);
        try {
            route(update);
            exchange.sendResponseHeaders(200, -1);
        } catch (Exception ex) {
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }

    private void route(IncomingUpdate update) throws Exception {
        if (update.message == null || update.message.body == null || update.message.recipient == null) {
            return;
        }

        String text = update.message.body.text;
        long chatId = update.message.recipient.chatId;

        if ("/help".equals(text)) {
            sendMessage(chatId, "Команды:\n/help\n/echo <текст>");
            return;
        }

        if (text != null && text.startsWith("/echo ")) {
            sendMessage(chatId, text.substring("/echo ".length()).trim());
            return;
        }

        if (text != null) {
            sendMessage(chatId, "Эхо: " + text);
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

