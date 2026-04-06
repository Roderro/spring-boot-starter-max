package ru.maxbot.starter.inbound;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.client.MaxBotHttpClient;
import ru.maxbot.core.dispatcher.UpdateDispatcher;
import ru.maxbot.core.mapper.UpdateMapper;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.transport.IncomingUpdate;
import ru.maxbot.core.transport.IncomingUpdateList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

public class LongPollingUpdateReceiver implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(LongPollingUpdateReceiver.class);

    private final MaxBotHttpClient client;
    private final UpdateDispatcher dispatcher;
    private final UpdateMapper updateMapper;
    private final MaxApi maxApi;
    private final int timeout;
    private final int limit;

    private volatile boolean running;
    private volatile Thread pollingThread;

    public LongPollingUpdateReceiver(MaxBotHttpClient client, UpdateDispatcher dispatcher,
                             UpdateMapper updateMapper, MaxApi maxApi,
                             int timeout, int limit) {
        this.client = client;
        this.dispatcher = dispatcher;
        this.updateMapper = updateMapper;
        this.maxApi = maxApi;
        this.timeout = timeout;
        this.limit = limit;
    }

    @Override
    public void start() {
        if (running) return;
        running = true;
        pollingThread = new Thread(this::poll, "max-bot-polling");
        pollingThread.setDaemon(true);
        pollingThread.start();
        log.info("MAX Bot long polling started (timeout={}s, limit={})", timeout, limit);
    }

    @Override
    public void stop() {
        running = false;
        if (pollingThread != null) {
            pollingThread.interrupt();
            try {
                pollingThread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        log.info("MAX Bot long polling stopped");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        return Integer.MAX_VALUE;
    }

    private void poll() {
        Long marker = null;
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                IncomingUpdateList result = client.getUpdates(marker, timeout, limit);

                if (result.getUpdates() != null) {
                    for (IncomingUpdate apiUpdate : result.getUpdates()) {
                        try {
                            Update update = updateMapper.map(apiUpdate);
                            dispatcher.dispatch(maxApi, update);
                        } catch (Exception e) {
                            log.error("Error dispatching update", e);
                        }
                    }
                }

                marker = result.getMarker();
            } catch (Exception e) {
                if (!running || Thread.currentThread().isInterrupted()) break;
                log.error("Long polling error, retrying in 3s", e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}


