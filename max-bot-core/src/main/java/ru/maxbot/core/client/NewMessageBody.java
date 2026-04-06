package ru.maxbot.core.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.maxbot.core.outgoing.AttachmentRequest;

public class NewMessageBody {

    private final String text;
    private final List<AttachmentRequest> attachments;
    private final NewMessageLink link;
    private Boolean notify;
    private String format;

    public NewMessageBody(@JsonProperty("text") String text,
                          @JsonProperty("attachments") List<AttachmentRequest> attachments,
                          @JsonProperty("link") NewMessageLink link) {
        this.text = text;
        this.attachments = attachments;
        this.link = link;
    }

    public String getText() {
        return text;
    }

    public List<AttachmentRequest> getAttachments() {
        return attachments;
    }

    public NewMessageLink getLink() {
        return link;
    }

    public NewMessageBody notify(Boolean notify) {
        this.notify = notify;
        return this;
    }

    public Boolean isNotify() {
        return notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    public NewMessageBody format(String format) {
        this.format = format;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}

