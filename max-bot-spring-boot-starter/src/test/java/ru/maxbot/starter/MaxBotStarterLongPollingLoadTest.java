package ru.maxbot.starter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import ru.maxbot.core.UpdateContext;
import ru.maxbot.starter.annotations.MaxController;
import ru.maxbot.starter.annotations.MessageRequest;
import ru.maxbot.starter.inbound.MaxBotInboundAutoConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MaxBotStarterLongPollingLoadTest {

    private static final int MAX_API_RPS = 30;
    private static final int UPDATE_COUNT = 1000;
    private static final int POLLING_LIMIT = 30;

    @Test
    void longPollingPipelineKeepsUpWithThirtyRpsMaxApiLimit() throws Exception {
        LoadScenarioResult result = runScenario();
        writeReport(result);
    }

    private LoadScenarioResult runScenario() throws Exception {
        LoadMetrics metrics = new LoadMetrics(UPDATE_COUNT);
        try (MockMaxApiServer server = MockMaxApiServer.start(MAX_API_RPS, UPDATE_COUNT)) {
            String previousEndpoint = System.getProperty("max.botapi.endpoint");
            System.setProperty("max.botapi.endpoint", server.endpoint());
            try (var context = new AnnotationConfigApplicationContext()) {
                TestPropertyValues.of(
                        "max.bot.access-token=test-token",
                        "max.bot.webhook.enabled=false",
                        "max.bot.polling.timeout=0",
                        "max.bot.polling.limit=" + POLLING_LIMIT,
                        "max.bot.rate-limit.requests-per-second=" + MAX_API_RPS
                ).applyTo(context);

                context.registerBean(LoadMetrics.class, () -> metrics);
                context.registerBean(ObjectMapper.class, MaxBotAutoConfiguration::createObjectMapper);
                context.register(MaxBotAutoConfiguration.class);
                context.register(MaxBotAnnotationAutoConfiguration.class);
                context.register(MaxBotDispatchAutoConfiguration.class);
                context.register(MaxBotInboundAutoConfiguration.class);
                context.register(LoadBotController.class);
                context.refresh();

                assertTrue(metrics.await(Duration.ofSeconds(60)),
                        "Long polling did not process all updates");
            } finally {
                if (previousEndpoint == null) {
                    System.clearProperty("max.botapi.endpoint");
                } else {
                    System.setProperty("max.botapi.endpoint", previousEndpoint);
                }
            }

            LoadScenarioResult result = new LoadScenarioResult(
                    POLLING_LIMIT,
                    UPDATE_COUNT,
                    server.acceptedRequests(),
                    server.rejectedRequests(),
                    server.updateRequests(),
                    server.messageRequests(),
                    server.maxObservedRequestsPerSecond(),
                    server.processingDuration(),
                    metrics.averageEndToEndMillis(),
                    metrics.maxEndToEndMillis()
            );

            assertEquals(0, result.rejectedApiRequests());
            assertEquals(UPDATE_COUNT, result.processedUpdates());
            assertTrue(result.maxObservedApiRps() <= MAX_API_RPS,
                    "Mock MAX API observed more than " + MAX_API_RPS + " rps");
            assertTrue(result.updatesPerSecond() >= result.expectedUpdatesPerSecond() * 0.8,
                    "Starter long polling throughput is below 80% of API-limited throughput");
            return result;
        }
    }

    private void writeReport(LoadScenarioResult result) throws IOException {
        Path report = Path.of("..", "vkr", "max-bot-long-polling-load-test.csv").normalize();
        Files.createDirectories(report.getParent());
        List<String> lines = new ArrayList<>();
        lines.add("pollingLimit,updates,apiRequests,maxObservedApiRps,updatesPerSecond,avgEndToEndMs");
        lines.add(String.format(java.util.Locale.ROOT,
                "%d,%d,%d,%d,%.2f,%.2f",
                result.pollingLimit(),
                result.processedUpdates(),
                result.acceptedApiRequests(),
                result.maxObservedApiRps(),
                result.updatesPerSecond(),
                result.averageEndToEndMillis()));
        Files.write(report, lines, StandardCharsets.UTF_8);
    }

    @MaxController
    static class LoadBotController {

        private final LoadMetrics metrics;

        LoadBotController(LoadMetrics metrics) {
            this.metrics = metrics;
        }

        @MessageRequest(textRegex = "load:.*")
        public void echo(UpdateContext ctx) {
            long startedNanos = Long.parseLong(ctx.text().substring("load:".length()));
            try {
                ctx.reply("ok");
            } finally {
                metrics.record(System.nanoTime() - startedNanos);
            }
        }
    }

    static final class LoadMetrics {

        private final CountDownLatch latch;
        private final LongAdder totalEndToEndNanos = new LongAdder();
        private final AtomicLong maxEndToEndNanos = new AtomicLong();
        private final AtomicInteger processedUpdates = new AtomicInteger();

        LoadMetrics(int expectedUpdates) {
            this.latch = new CountDownLatch(expectedUpdates);
        }

        void record(long endToEndNanos) {
            totalEndToEndNanos.add(endToEndNanos);
            maxEndToEndNanos.accumulateAndGet(endToEndNanos, Math::max);
            processedUpdates.incrementAndGet();
            latch.countDown();
        }

        boolean await(Duration timeout) throws InterruptedException {
            return latch.await(timeout.toMillis(), TimeUnit.MILLISECONDS);
        }

        double averageEndToEndMillis() {
            long processed = processedUpdates();
            if (processed == 0) {
                return 0.0;
            }
            return totalEndToEndNanos.sum() / 1_000_000.0 / processed;
        }

        double maxEndToEndMillis() {
            return maxEndToEndNanos.get() / 1_000_000.0;
        }

        long processedUpdates() {
            return processedUpdates.get();
        }
    }

    record LoadScenarioResult(
            int pollingLimit,
            int processedUpdates,
            int acceptedApiRequests,
            int rejectedApiRequests,
            int updateRequests,
            int messageRequests,
            int maxObservedApiRps,
            Duration duration,
            double averageEndToEndMillis,
            double maxEndToEndMillis
    ) {
        double updatesPerSecond() {
            return processedUpdates / Math.max(0.001, duration.toMillis() / 1000.0);
        }

        double expectedUpdatesPerSecond() {
            return MAX_API_RPS / (1.0 + (1.0 / pollingLimit));
        }
    }

    private static final class MockMaxApiServer implements AutoCloseable {

        private final HttpServer server;
        private final ExecutorService executor;
        private final int maxRequestsPerSecond;
        private final int updateCount;
        private final Deque<Long> requestWindow = new ArrayDeque<>();
        private final AtomicInteger acceptedRequests = new AtomicInteger();
        private final AtomicInteger rejectedRequests = new AtomicInteger();
        private final AtomicInteger updateRequests = new AtomicInteger();
        private final AtomicInteger messageRequests = new AtomicInteger();
        private final AtomicLong firstAcceptedRequestNanos = new AtomicLong();
        private final AtomicLong lastMessageRequestNanos = new AtomicLong();
        private int nextUpdateIndex;
        private int maxObservedRequestsPerSecond;

        private MockMaxApiServer(HttpServer server, ExecutorService executor,
                                 int maxRequestsPerSecond, int updateCount) {
            this.server = server;
            this.executor = executor;
            this.maxRequestsPerSecond = maxRequestsPerSecond;
            this.updateCount = updateCount;
        }

        static MockMaxApiServer start(int maxRequestsPerSecond, int updateCount) throws IOException {
            HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            ExecutorService executor = Executors.newFixedThreadPool(4);
            MockMaxApiServer mock = new MockMaxApiServer(httpServer, executor,
                    maxRequestsPerSecond, updateCount);
            httpServer.createContext("/updates", mock::handleUpdates);
            httpServer.createContext("/messages", mock::handleMessages);
            httpServer.setExecutor(executor);
            httpServer.start();
            return mock;
        }

        String endpoint() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        int acceptedRequests() {
            return acceptedRequests.get();
        }

        int rejectedRequests() {
            return rejectedRequests.get();
        }

        int updateRequests() {
            return updateRequests.get();
        }

        int messageRequests() {
            return messageRequests.get();
        }

        synchronized int maxObservedRequestsPerSecond() {
            return maxObservedRequestsPerSecond;
        }

        Duration processingDuration() {
            long started = firstAcceptedRequestNanos.get();
            long finished = lastMessageRequestNanos.get();
            if (started == 0L || finished == 0L || finished <= started) {
                return Duration.ZERO;
            }
            return Duration.ofNanos(finished - started);
        }

        private void handleUpdates(HttpExchange exchange) throws IOException {
            if (!registerRequestAndCheckLimit()) {
                rejectedRequests.incrementAndGet();
                send(exchange, 429, "{\"message\":\"rate limit exceeded\"}");
                return;
            }
            acceptedRequests.incrementAndGet();
            markFirstAcceptedRequest();
            updateRequests.incrementAndGet();

            int limit = parseIntQueryParam(exchange.getRequestURI(), "limit", 100);
            List<String> batch = nextUpdateBatch(limit);
            String body = "{\"updates\":[" + String.join(",", batch) + "],\"marker\":" + nextUpdateIndex + "}";
            send(exchange, 200, body);
        }

        private void handleMessages(HttpExchange exchange) throws IOException {
            drainRequestBody(exchange);
            if (!"POST".equals(exchange.getRequestMethod())) {
                send(exchange, 405, "");
                return;
            }
            if (!registerRequestAndCheckLimit()) {
                rejectedRequests.incrementAndGet();
                send(exchange, 429, "{\"message\":\"rate limit exceeded\"}");
                return;
            }
            acceptedRequests.incrementAndGet();
            markFirstAcceptedRequest();
            lastMessageRequestNanos.set(System.nanoTime());
            messageRequests.incrementAndGet();
            send(exchange, 200, "");
        }

        private void markFirstAcceptedRequest() {
            firstAcceptedRequestNanos.compareAndSet(0L, System.nanoTime());
        }

        private synchronized List<String> nextUpdateBatch(int limit) {
            int end = Math.min(nextUpdateIndex + limit, updateCount);
            List<String> batch = new ArrayList<>(end - nextUpdateIndex);
            for (int i = nextUpdateIndex; i < end; i++) {
                batch.add(createUpdate(i));
            }
            nextUpdateIndex = end;
            return batch;
        }

        private synchronized boolean registerRequestAndCheckLimit() {
            long now = System.nanoTime();
            long windowStart = now - 1_000_000_000L;
            while (!requestWindow.isEmpty() && requestWindow.peekFirst() <= windowStart) {
                requestWindow.removeFirst();
            }
            requestWindow.addLast(now);
            maxObservedRequestsPerSecond = Math.max(maxObservedRequestsPerSecond, requestWindow.size());
            return requestWindow.size() <= maxRequestsPerSecond;
        }

        private static String createUpdate(int index) {
            return """
                        {
                          "message": {
                            "recipient": {"chat_id": 10001, "chat_type": "dialog", "user_id": 20002},
                            "timestamp": 1700000000000,
                            "sender": {"user_id": 30003, "first_name": "Load", "is_bot": false},
                            "message": {"mid": "mid.load.%d", "seq": %d, "text": "load:%d"}
                          },
                          "timestamp": 1700000000000,
                          "user_locale": "ru",
                          "update_type": "message_created"
                        }
                        """.formatted(index, index, System.nanoTime());
        }

        private int parseIntQueryParam(URI uri, String name, int defaultValue) {
            Map<String, String> query = parseQuery(uri.getRawQuery());
            String value = query.get(name);
            if (value == null || value.isBlank()) {
                return defaultValue;
            }
            return Integer.parseInt(value);
        }

        private Map<String, String> parseQuery(String rawQuery) {
            Map<String, String> result = new LinkedHashMap<>();
            if (rawQuery == null || rawQuery.isBlank()) {
                return result;
            }
            for (String pair : rawQuery.split("&")) {
                int separator = pair.indexOf('=');
                if (separator < 0) {
                    result.put(decode(pair), "");
                } else {
                    result.put(decode(pair.substring(0, separator)),
                            decode(pair.substring(separator + 1)));
                }
            }
            return result;
        }

        private String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }

        private void drainRequestBody(HttpExchange exchange) throws IOException {
            exchange.getRequestBody().readAllBytes();
        }

        private void send(HttpExchange exchange, int status, String body) throws IOException {
            byte[] payload = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, payload.length);
            exchange.getResponseBody().write(payload);
            exchange.close();
        }

        @Override
        public void close() {
            server.stop(0);
            executor.shutdownNow();
        }
    }
}
