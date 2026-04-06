package ru.maxbot.experiment.rawapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class RawApiSecurityWebhookServer {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RawApiSecurityBot bot;

    public RawApiSecurityWebhookServer(RawApiSecurityBot bot) {
        this.bot = bot;
    }

    public static void main(String[] args) throws Exception {
        String token = System.getenv("MAX_BOT_TOKEN");
        RawApiSecurityBot bot = new RawApiSecurityBot(token);
        new RawApiSecurityWebhookServer(bot).start(8080, "/webhook");
    }

    public void start(int port, String path) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(path, this::handleWebhook);
        server.start();
    }

    private void handleWebhook(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            exchange.close();
            return;
        }

        try {
            RawApiSecurityBot.IncomingUpdate update =
                    mapper.readValue(exchange.getRequestBody(), RawApiSecurityBot.IncomingUpdate.class);
            bot.handle(update);
            exchange.sendResponseHeaders(200, -1);
        } catch (Exception ex) {
            exchange.sendResponseHeaders(500, -1);
        } finally {
            exchange.close();
        }
    }
}

