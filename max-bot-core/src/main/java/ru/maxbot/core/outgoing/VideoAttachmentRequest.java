package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class VideoAttachmentRequest extends AttachmentRequest {

    private final UploadedInfo payload;

    public VideoAttachmentRequest(@JsonProperty("payload") UploadedInfo payload) {
        super(VIDEO);
        this.payload = payload;
    }

    public UploadedInfo getPayload() {
        return payload;
    }
}

