package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class PhotoAttachmentRequest extends AttachmentRequest {

    private final PhotoAttachmentRequestPayload payload;

    public PhotoAttachmentRequest(@JsonProperty("payload") PhotoAttachmentRequestPayload payload) {
        super(IMAGE);
        this.payload = payload;
    }

    public PhotoAttachmentRequestPayload getPayload() {
        return payload;
    }
}

