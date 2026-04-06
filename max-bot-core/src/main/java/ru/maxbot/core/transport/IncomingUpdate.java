package ru.maxbot.core.transport;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.maxbot.core.model.User;

public class IncomingUpdate {

    private IncomingMessage message;
    private IncomingCallback callback;
    @JsonProperty("chat_id")
    private Long chatId;
    @JsonProperty("message_id")
    private String messageId;
    private User user;
    private String payload;
    private String title;
    private IncomingChat chat;
    @JsonProperty("start_payload")
    private String startPayload;
    private Long timestamp;
    @JsonProperty("user_locale")
    private String userLocale;
    @JsonProperty("update_type")
    private String updateType;

    public IncomingMessage getMessage() {
        return message;
    }

    public void setMessage(IncomingMessage message) {
        this.message = message;
    }

    public IncomingCallback getCallback() {
        return callback;
    }

    public void setCallback(IncomingCallback callback) {
        this.callback = callback;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public IncomingChat getChat() {
        return chat;
    }

    public void setChat(IncomingChat chat) {
        this.chat = chat;
    }

    public String getStartPayload() {
        return startPayload;
    }

    public void setStartPayload(String startPayload) {
        this.startPayload = startPayload;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserLocale() {
        return userLocale;
    }

    public void setUserLocale(String userLocale) {
        this.userLocale = userLocale;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }
}

