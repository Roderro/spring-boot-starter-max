package ru.maxbot.core.retry;

import java.util.concurrent.atomic.AtomicInteger;

import ru.maxbot.core.exception.MaxBotException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RetryPolicyTest {

    @Test
    void succeedsOnFirstAttempt() {
        var policy = new RetryPolicy(3, 10, 2.0);
        String result = policy.execute(() -> "ok");
        assertEquals("ok", result);
    }

    @Test
    void retriesAndSucceeds() {
        var counter = new AtomicInteger(0);
        var policy = new RetryPolicy(3, 10, 1.0);

        String result = policy.execute(() -> {
            if (counter.incrementAndGet() < 3) {
                throw new RuntimeException("fail");
            }
            return "ok";
        });

        assertEquals("ok", result);
        assertEquals(3, counter.get());
    }

    @Test
    void throwsAfterAllAttemptsFailed() {
        var policy = new RetryPolicy(2, 10, 1.0);

        assertThrows(MaxBotException.class, () ->
                policy.execute(() -> {
                    throw new RuntimeException("always fail");
                }));
    }

    @Test
    void noRetryExecutesOnce() {
        var counter = new AtomicInteger(0);
        var policy = RetryPolicy.noRetry();

        assertThrows(MaxBotException.class, () ->
                policy.execute(() -> {
                    counter.incrementAndGet();
                    throw new RuntimeException("fail");
                }));

        assertEquals(1, counter.get());
    }
}


