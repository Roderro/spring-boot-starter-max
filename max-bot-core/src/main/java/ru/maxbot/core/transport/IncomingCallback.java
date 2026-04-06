package ru.maxbot.core.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.maxbot.core.model.User;

public class IncomingCallback {

    @JsonProperty("callback_id")
    private String callbackId;
    private String payload;
    private User user;

    public String getCallbackId() {
        return callbackId;
    }

    public void setCallbackId(String callbackId) {
        this.callbackId = callbackId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

