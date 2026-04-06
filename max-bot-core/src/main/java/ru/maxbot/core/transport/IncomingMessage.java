package ru.maxbot.core.transport;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.maxbot.core.model.User;

public class IncomingMessage {

    private IncomingRecipient recipient;
    private Long timestamp;
    private User sender;
    @JsonAlias("message")
    @JsonProperty("body")
    private IncomingMessageBody body;
    private String url;

    public IncomingRecipient getRecipient() {
        return recipient;
    }

    public void setRecipient(IncomingRecipient recipient) {
        this.recipient = recipient;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public IncomingMessageBody getBody() {
        return body;
    }

    public void setBody(IncomingMessageBody body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

