package ru.maxbot.core.transport;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncomingChat {

    @JsonProperty("chat_id")
    private Long chatId;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }
}

