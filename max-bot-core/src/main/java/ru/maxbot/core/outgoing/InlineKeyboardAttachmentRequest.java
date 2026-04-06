package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class InlineKeyboardAttachmentRequest extends AttachmentRequest {

    private final InlineKeyboardAttachmentRequestPayload payload;

    public InlineKeyboardAttachmentRequest(@JsonProperty("payload") InlineKeyboardAttachmentRequestPayload payload) {
        super(INLINE_KEYBOARD);
        this.payload = payload;
    }

    public InlineKeyboardAttachmentRequestPayload getPayload() {
        return payload;
    }
}

