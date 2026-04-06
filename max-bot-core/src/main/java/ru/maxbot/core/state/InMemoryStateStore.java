package ru.maxbot.core.state;

import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryStateStore implements StateStore {

    private final ConcurrentHashMap<Long, String> states = new ConcurrentHashMap<>();

    @Override
    public String getState(long chatId) {
        return states.get(chatId);
    }

    @Override
    public void setState(long chatId, String state) {
        if (state == null) {
            states.remove(chatId);
        } else {
            states.put(chatId, state);
        }
    }

    @Override
    public void clearState(long chatId) {
        states.remove(chatId);
    }
}


