package ru.maxbot.core.retry;

import java.util.function.Supplier;

import ru.maxbot.core.exception.MaxBotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RetryPolicy {

    private static final Logger log = LoggerFactory.getLogger(RetryPolicy.class);

    private final int maxAttempts;
    private final long initialDelayMs;
    private final double multiplier;

    public RetryPolicy(int maxAttempts, long initialDelayMs, double multiplier) {
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialDelayMs = Math.max(0, initialDelayMs);
        this.multiplier = Math.max(1.0, multiplier);
    }

    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(3, 500, 2.0);
    }

    public static RetryPolicy noRetry() {
        return new RetryPolicy(1, 0, 1.0);
    }

    public <T> T execute(Supplier<T> action) {
        Exception lastException = null;
        long delay = initialDelayMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return action.get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    log.warn("Attempt {}/{} failed, retrying in {}ms: {}",
                            attempt, maxAttempts, delay, e.getMessage());
                    sleep(delay);
                    delay = (long) (delay * multiplier);
                }
            }
        }

        throw new MaxBotException("All " + maxAttempts + " attempts failed", lastException);
    }

    public void executeVoid(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MaxBotException("Retry interrupted", e);
        }
    }

    public int maxAttempts() {
        return maxAttempts;
    }

    public long initialDelayMs() {
        return initialDelayMs;
    }

    public double multiplier() {
        return multiplier;
    }
}


