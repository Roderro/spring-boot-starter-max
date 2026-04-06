package ru.maxbot.core.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Chat {

    @JsonProperty("chat_id")
    private final Long chatId;
    private final ChatType type;
    private final ChatStatus status;
    private final String title;
    private final Image icon;
    @JsonProperty("last_event_time")
    private final Long lastEventTime;
    @JsonProperty("participants_count")
    private final Integer participantsCount;
    @JsonProperty("is_public")
    private final Boolean isPublic;
    private String link;
    @JsonProperty("owner_id")
    private Long ownerId;
    private Map<String, Long> participants;
    private String description;
    @JsonProperty("messages_count")
    private Integer messagesCount;
    @JsonProperty("chat_message_id")
    private String chatMessageId;

    public Chat(@JsonProperty("chat_id") Long chatId,
                @JsonProperty("type") ChatType type,
                @JsonProperty("status") ChatStatus status,
                @JsonProperty("title") String title,
                @JsonProperty("icon") Image icon,
                @JsonProperty("last_event_time") Long lastEventTime,
                @JsonProperty("participants_count") Integer participantsCount,
                @JsonProperty("is_public") Boolean isPublic,
                @JsonProperty("link") String link) {
        this.chatId = chatId;
        this.type = type;
        this.status = status;
        this.title = title;
        this.icon = icon;
        this.lastEventTime = lastEventTime;
        this.participantsCount = participantsCount;
        this.isPublic = isPublic;
        this.link = link;
    }

    public Long getChatId() {
        return chatId;
    }

    public ChatType getType() {
        return type;
    }

    public ChatStatus getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public Image getIcon() {
        return icon;
    }

    public Long getLastEventTime() {
        return lastEventTime;
    }

    public Integer getParticipantsCount() {
        return participantsCount;
    }

    public Boolean isPublic() {
        return isPublic;
    }

    public Chat ownerId(Long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Chat participants(Map<String, Long> participants) {
        this.participants = participants;
        return this;
    }

    public Map<String, Long> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, Long> participants) {
        this.participants = participants;
    }

    public Chat link(String link) {
        this.link = link;
        return this;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Chat messagesCount(Integer messagesCount) {
        this.messagesCount = messagesCount;
        return this;
    }

    public Integer getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(Integer messagesCount) {
        this.messagesCount = messagesCount;
    }

    public Chat chatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
        return this;
    }

    public String getChatMessageId() {
        return chatMessageId;
    }

    public void setChatMessageId(String chatMessageId) {
        this.chatMessageId = chatMessageId;
    }
}

