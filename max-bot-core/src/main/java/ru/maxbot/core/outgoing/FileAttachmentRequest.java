package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class FileAttachmentRequest extends AttachmentRequest {

    private final UploadedInfo payload;

    public FileAttachmentRequest(@JsonProperty("payload") UploadedInfo payload) {
        super(FILE);
        this.payload = payload;
    }

    public UploadedInfo getPayload() {
        return payload;
    }
}

