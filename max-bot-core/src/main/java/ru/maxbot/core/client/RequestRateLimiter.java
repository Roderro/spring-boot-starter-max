package ru.maxbot.core.client;

import java.util.concurrent.TimeUnit;

import ru.maxbot.core.exception.MaxBotException;

final class RequestRateLimiter {

    private final long intervalNanos;
    private long nextAllowedNanos;

    RequestRateLimiter(int requestsPerSecond) {
        if (requestsPerSecond <= 0) {
            this.intervalNanos = 0L;
        } else {
            this.intervalNanos = Math.max(1L,
                    (long) Math.ceil(1_000_000_000.0 / requestsPerSecond));
        }
    }

    synchronized void acquire() {
        if (intervalNanos == 0L) {
            return;
        }

        long now = System.nanoTime();
        if (nextAllowedNanos > now) {
            sleep(nextAllowedNanos - now);
            now = System.nanoTime();
        }
        nextAllowedNanos = Math.max(now, nextAllowedNanos) + intervalNanos;
    }

    private void sleep(long nanos) {
        try {
            TimeUnit.NANOSECONDS.sleep(nanos);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MaxBotException("Interrupted while waiting for MAX API request rate limit", e);
        }
    }
}
