package ru.maxbot.starter.actuator;

import ru.maxbot.core.UpdateContext;
import ru.maxbot.core.interceptor.HandlerInterceptor;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class MetricsHandlerInterceptor implements HandlerInterceptor {

    private static final String UPDATES_COUNTER = "maxbot.updates.received";
    private static final String HANDLER_TIMER = "maxbot.handler.duration";
    private static final String ERRORS_COUNTER = "maxbot.handler.errors";

    private final MeterRegistry meterRegistry;
    private final ThreadLocal<Timer.Sample> samples = new ThreadLocal<>();

    public MetricsHandlerInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public boolean preHandle(UpdateContext ctx) {
        String type = ctx.update().type().name();
        Counter.builder(UPDATES_COUNTER)
                .tag("type", type)
                .register(meterRegistry)
                .increment();
        samples.set(Timer.start(meterRegistry));
        return true;
    }

    @Override
    public void postHandle(UpdateContext ctx) {
        stopTimer(ctx);
    }

    @Override
    public void afterCompletion(UpdateContext ctx, Exception ex) {
        if (ex != null) {
            String type = ctx.update().type().name();
            Counter.builder(ERRORS_COUNTER)
                    .tag("type", type)
                    .tag("exception", ex.getClass().getSimpleName())
                    .register(meterRegistry)
                    .increment();
        }
        stopTimer(ctx);
    }

    private void stopTimer(UpdateContext ctx) {
        Timer.Sample sample = samples.get();
        if (sample != null) {
            String type = ctx.update().type().name();
            sample.stop(Timer.builder(HANDLER_TIMER)
                    .tag("type", type)
                    .register(meterRegistry));
            samples.remove();
        }
    }
}


