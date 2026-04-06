package ru.maxbot.core.state;

public interface StateStore {

    String getState(long chatId);

    void setState(long chatId, String state);

    void clearState(long chatId);
}


