package ru.maxbot.core.outgoing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class OutgoingMessage {

    private final String text;
    private final List<List<Button>> keyboard;
    private final Format format;
    private final boolean notify;
    private final String replyToMessageId;
    private final List<AttachmentRequest> attachments;

    private OutgoingMessage(Builder builder) {
        this.text = builder.text;
        this.keyboard = builder.keyboard != null ? copyKeyboard(builder.keyboard) : null;
        this.format = builder.format;
        this.notify = builder.notify;
        this.replyToMessageId = builder.replyToMessageId;
        this.attachments = builder.attachments != null
                ? List.copyOf(builder.attachments)
                : null;
    }

    public String text() {
        return text;
    }

    public List<List<Button>> keyboard() {
        return keyboard;
    }

    public Format format() {
        return format;
    }

    public boolean shouldNotify() {
        return notify;
    }

    public String replyToMessageId() {
        return replyToMessageId;
    }

    public List<AttachmentRequest> attachments() {
        return attachments;
    }

    public static Builder text(String text) {
        return new Builder(text);
    }

    public static Button callbackButton(String text, String payload) {
        return new CallbackButton(payload, text);
    }

    public static Button linkButton(String text, String url) {
        return new LinkButton(url, text);
    }

    public static AttachmentRequest photo(String token) {
        return new PhotoAttachmentRequest(new PhotoAttachmentRequestPayload().token(token));
    }

    public static AttachmentRequest video(String token) {
        return new VideoAttachmentRequest(new UploadedInfo().token(token));
    }

    public static AttachmentRequest audio(String token) {
        return new AudioAttachmentRequest(new UploadedInfo().token(token));
    }

    public static AttachmentRequest file(String token) {
        return new FileAttachmentRequest(new UploadedInfo().token(token));
    }

    public static List<List<Button>> keyboard(List<List<Button>> rows) {
        return copyKeyboard(rows);
    }

    public static List<List<Button>> keyboardRow(Button... buttons) {
        return List.of(List.of(buttons));
    }

    public static InlineKeyboardAttachmentRequestPayload inlineKeyboardPayload(List<List<Button>> rows) {
        return new InlineKeyboardAttachmentRequestPayload(copyKeyboard(rows));
    }

    public enum Format {
        MARKDOWN, HTML
    }

    public static final class Builder {
        private final String text;
        private List<List<Button>> keyboard;
        private Format format;
        private boolean notify = true;
        private String replyToMessageId;
        private List<AttachmentRequest> attachments;

        private Builder(String text) {
            this.text = text;
        }

        public Builder keyboard(List<List<Button>> keyboard) {
            this.keyboard = copyKeyboard(keyboard);
            return this;
        }

        public Builder keyboardRow(Button... buttons) {
            if (this.keyboard == null) {
                this.keyboard = new ArrayList<>();
            }
            this.keyboard.add(List.of(buttons));
            return this;
        }

        public Builder format(Format format) {
            this.format = format;
            return this;
        }

        public Builder markdown() {
            this.format = Format.MARKDOWN;
            return this;
        }

        public Builder html() {
            this.format = Format.HTML;
            return this;
        }

        public Builder disableNotify() {
            this.notify = false;
            return this;
        }

        public Builder replyTo(String messageId) {
            this.replyToMessageId = messageId;
            return this;
        }

        public Builder attach(AttachmentRequest attachment) {
            if (this.attachments == null) {
                this.attachments = new ArrayList<>();
            }
            this.attachments.add(attachment);
            return this;
        }

        public Builder attachments(List<AttachmentRequest> attachments) {
            this.attachments = new ArrayList<>(attachments);
            return this;
        }

        public OutgoingMessage build() {
            return new OutgoingMessage(this);
        }
    }

    private static List<List<Button>> copyKeyboard(List<List<Button>> keyboard) {
        List<List<Button>> copy = new ArrayList<>(keyboard.size());
        for (List<Button> row : keyboard) {
            copy.add(Collections.unmodifiableList(new ArrayList<>(row)));
        }
        return Collections.unmodifiableList(copy);
    }
}


