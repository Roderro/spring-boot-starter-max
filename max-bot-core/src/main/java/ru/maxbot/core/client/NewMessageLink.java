package ru.maxbot.core.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NewMessageLink {

    private final String type;
    @JsonProperty("mid")
    private final String messageId;

    public NewMessageLink(@JsonProperty("type") String type,
                          @JsonProperty("mid") String messageId) {
        this.type = type;
        this.messageId = messageId;
    }

    public String getType() {
        return type;
    }

    public String getMessageId() {
        return messageId;
    }
}

