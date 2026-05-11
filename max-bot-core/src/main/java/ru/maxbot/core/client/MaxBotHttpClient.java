package ru.maxbot.core.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.maxbot.core.exception.MaxBotException;
import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.UploadedInfo;
import ru.maxbot.core.transport.IncomingUpdateList;

public final class MaxBotHttpClient {

    private static final String DEFAULT_ENDPOINT = "https://platform-api.max.ru";

    private final String accessToken;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String endpoint;
    private final RequestRateLimiter rateLimiter;

    public MaxBotHttpClient(String accessToken, HttpClient httpClient, ObjectMapper objectMapper) {
        this(accessToken, httpClient, objectMapper, 30);
    }

    public MaxBotHttpClient(String accessToken, HttpClient httpClient, ObjectMapper objectMapper,
                            int maxRequestsPerSecond) {
        this.accessToken = accessToken;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper.copy()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.endpoint = resolveEndpoint();
        this.rateLimiter = new RequestRateLimiter(maxRequestsPerSecond);
    }

    public IncomingUpdateList getUpdates(Long marker, Integer timeout, Integer limit) {
        Map<String, Object> query = new LinkedHashMap<>();
        query.put("marker", marker);
        query.put("timeout", timeout);
        query.put("limit", limit);
        return get("/updates", query, IncomingUpdateList.class);
    }

    public User getMe() {
        return get("/me", Map.of(), User.class);
    }

    public Chat getChat(long chatId) {
        return get("/chats/" + chatId, Map.of(), Chat.class);
    }

    public ChatMembersList getChatMembers(long chatId) {
        return get("/chats/" + chatId + "/members", Map.of(), ChatMembersList.class);
    }

    public void leaveChat(long chatId) {
        exchange("DELETE", "/chats/" + chatId + "/members/me", Map.of(), null, Void.class);
    }

    public void sendMessage(NewMessageBody body, long chatId) {
        exchange("POST", "/messages", Map.of("chat_id", chatId), body, Void.class);
    }

    public void editMessage(NewMessageBody body, String messageId) {
        exchange("PUT", "/messages", Map.of("message_id", messageId), body, Void.class);
    }

    public void deleteMessage(String messageId) {
        exchange("DELETE", "/messages", Map.of("message_id", messageId), null, Void.class);
    }

    public void answerCallback(CallbackAnswer answer, String callbackId) {
        exchange("POST", "/answers", Map.of("callback_id", callbackId), answer, Void.class);
    }

    public UploadEndpoint getUploadUrl(UploadType type) {
        return exchange("POST", "/uploads", Map.of("type", type.getValue()), null, UploadEndpoint.class);
    }

    public UploadedInfo uploadFile(String url, File file) {
        String boundary = "----MaxBotBoundary" + UUID.randomUUID().toString().replace("-", "");
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(2))
                    .header("Authorization", accessToken)
                    .header("Accept", "application/json")
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(HttpRequest.BodyPublishers.ofByteArray(buildMultipartBody(boundary, file)))
                    .build();
        } catch (IOException e) {
            throw new MaxBotException("Unable to read upload file: " + file, e);
        }

        try {
            rateLimiter.acquire();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return handleResponse(response, UploadedInfo.class, url);
        } catch (IOException e) {
            throw new MaxBotException("Upload request failed: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MaxBotException("Upload request interrupted: " + url, e);
        }
    }

    private <T> T get(String path, Map<String, ?> query, Class<T> responseType) {
        return exchange("GET", path, query, null, responseType);
    }

    private <T> T exchange(String method, String path, Map<String, ?> query, Object body, Class<T> responseType) {
        String url = buildUrl(path, query);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Authorization", accessToken)
                .header("Accept", "application/json");
        if (body != null) {
            try {
                byte[] json = objectMapper.writeValueAsBytes(body);
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofByteArray(json));
            } catch (IOException e) {
                throw new MaxBotException("Unable to serialize request body for " + path, e);
            }
        } else {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        try {
            rateLimiter.acquire();
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            return handleResponse(response, responseType, url);
        } catch (IOException e) {
            throw new MaxBotException("HTTP request failed: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MaxBotException("HTTP request interrupted: " + url, e);
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseType, String url) {
        int status = response.statusCode();
        String body = response.body();
        if (status / 100 == 2) {
            if (responseType == Void.class || body == null || body.isBlank()) {
                return null;
            }
            try {
                return objectMapper.readValue(body, responseType);
            } catch (IOException e) {
                throw new MaxBotException("Unable to deserialize response from " + url, e);
            }
        }
        throw new MaxBotException("MAX API request failed: status=" + status + ", url=" + url + ", body=" + body);
    }

    private String buildUrl(String path, Map<String, ?> query) {
        StringBuilder builder = new StringBuilder();
        if (path.startsWith("http")) {
            builder.append(path);
        } else {
            builder.append(endpoint).append(path);
        }
        if (!query.isEmpty()) {
            builder.append(path.contains("?") ? '&' : '?');
        }

        boolean firstParam = !path.contains("?");
        for (Map.Entry<String, ?> entry : query.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            if (!firstParam) {
                builder.append('&');
            }
            builder
                    .append(entry.getKey())
                    .append('=')
                    .append(encode(String.valueOf(entry.getValue())));
            firstParam = false;
        }
        return builder.toString();
    }

    private byte[] buildMultipartBody(String boundary, File file) throws IOException {
        byte[] fileBytes = Files.readAllBytes(file.toPath());
        List<byte[]> parts = new ArrayList<>();
        parts.add(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + file.getName() + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Type: application/octet-stream\r\n\r\n").getBytes(StandardCharsets.UTF_8));
        parts.add(fileBytes);
        parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
        parts.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

        int totalSize = parts.stream().mapToInt(part -> part.length).sum();
        byte[] body = new byte[totalSize];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, body, offset, part.length);
            offset += part.length;
        }
        return body;
    }

    private String resolveEndpoint() {
        String envValue = System.getenv("MAX_BOTAPI_ENDPOINT");
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return System.getProperty("max.botapi.endpoint", DEFAULT_ENDPOINT);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}

