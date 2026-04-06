package ru.maxbot.core.transport;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IncomingRecipient {

    @JsonProperty("chat_id")
    private Long chatId;
    @JsonProperty("chat_type")
    private String chatType;
    @JsonProperty("user_id")
    private Long userId;

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getChatType() {
        return chatType;
    }

    public void setChatType(String chatType) {
        this.chatType = chatType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}

