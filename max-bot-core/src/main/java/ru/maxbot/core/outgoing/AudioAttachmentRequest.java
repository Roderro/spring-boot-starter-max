package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class AudioAttachmentRequest extends AttachmentRequest {

    private final UploadedInfo payload;

    public AudioAttachmentRequest(@JsonProperty("payload") UploadedInfo payload) {
        super(AUDIO);
        this.payload = payload;
    }

    public UploadedInfo getPayload() {
        return payload;
    }
}

