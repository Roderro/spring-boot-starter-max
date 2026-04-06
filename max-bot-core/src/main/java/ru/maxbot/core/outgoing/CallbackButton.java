package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class CallbackButton extends Button {

    private final String payload;

    public CallbackButton(@JsonProperty("payload") String payload,
                          @JsonProperty("text") String text) {
        super(text, CALLBACK);
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}

