package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PhotoAttachmentRequest.class, name = AttachmentRequest.IMAGE),
        @JsonSubTypes.Type(value = VideoAttachmentRequest.class, name = AttachmentRequest.VIDEO),
        @JsonSubTypes.Type(value = AudioAttachmentRequest.class, name = AttachmentRequest.AUDIO),
        @JsonSubTypes.Type(value = FileAttachmentRequest.class, name = AttachmentRequest.FILE),
        @JsonSubTypes.Type(value = InlineKeyboardAttachmentRequest.class, name = AttachmentRequest.INLINE_KEYBOARD)
})
public abstract class AttachmentRequest {

    public static final String IMAGE = "image";
    public static final String VIDEO = "video";
    public static final String AUDIO = "audio";
    public static final String FILE = "file";
    public static final String INLINE_KEYBOARD = "inline_keyboard";

    private final String type;

    protected AttachmentRequest(String type) {
        this.type = type;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }
}

