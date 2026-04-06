package ru.maxbot.core.outgoing;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class InlineKeyboardAttachmentRequestPayload {

    private final List<List<Button>> buttons;

    public InlineKeyboardAttachmentRequestPayload(@JsonProperty("buttons") List<List<Button>> buttons) {
        this.buttons = buttons;
    }

    public List<List<Button>> getButtons() {
        return buttons;
    }
}

