package ru.maxbot.core.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.client.CallbackAnswer;
import ru.maxbot.core.client.MaxBotHttpClient;
import ru.maxbot.core.client.NewMessageBody;
import ru.maxbot.core.client.NewMessageLink;
import ru.maxbot.core.client.UploadEndpoint;
import ru.maxbot.core.client.UploadType;
import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.ChatMember;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.AttachmentRequest;
import ru.maxbot.core.outgoing.InlineKeyboardAttachmentRequest;
import ru.maxbot.core.outgoing.InlineKeyboardAttachmentRequestPayload;
import ru.maxbot.core.outgoing.OutgoingMessage;
import ru.maxbot.core.retry.RetryPolicy;

public final class DefaultMaxApi implements MaxApi {

    private final MaxBotHttpClient client;
    private final RetryPolicy retryPolicy;

    public DefaultMaxApi(MaxBotHttpClient client) {
        this(client, RetryPolicy.noRetry());
    }

    public DefaultMaxApi(MaxBotHttpClient client, RetryPolicy retryPolicy) {
        this.client = client;
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.noRetry();
    }

    @Override
    public void sendMessage(long chatId, String text) {
        sendMessage(chatId, OutgoingMessage.text(text).build());
    }

    @Override
    public void sendMessage(long chatId, OutgoingMessage message) {
        client.sendMessage(toNewMessageBody(message), chatId);
    }

    @Override
    public void editMessage(String messageId, String text) {
        editMessage(messageId, OutgoingMessage.text(text).build());
    }

    @Override
    public void editMessage(String messageId, OutgoingMessage message) {
        client.editMessage(toNewMessageBody(message), messageId);
    }

    @Override
    public void deleteMessage(String messageId) {
        client.deleteMessage(messageId);
    }

    @Override
    public void answerCallback(String callbackId, String notification) {
        client.answerCallback(new CallbackAnswer().notification(notification), callbackId);
    }

    @Override
    public void answerCallback(String callbackId, OutgoingMessage message) {
        client.answerCallback(new CallbackAnswer().message(toNewMessageBody(message)), callbackId);
    }

    @Override
    public User getMe() {
        return client.getMe();
    }

    @Override
    public String uploadImage(File file) {
        return retryPolicy.execute(() -> upload(file, UploadType.IMAGE, true));
    }

    @Override
    public String uploadVideo(File file) {
        return retryPolicy.execute(() -> upload(file, UploadType.VIDEO, false));
    }

    @Override
    public String uploadAudio(File file) {
        return retryPolicy.execute(() -> upload(file, UploadType.AUDIO, false));
    }

    @Override
    public String uploadFile(File file) {
        return retryPolicy.execute(() -> upload(file, UploadType.FILE, true));
    }

    @Override
    public Chat getChat(long chatId) {
        return client.getChat(chatId);
    }

    @Override
    public List<ChatMember> getChatMembers(long chatId) {
        var membersList = client.getChatMembers(chatId);
        return membersList.getMembers() != null ? List.copyOf(membersList.getMembers()) : List.of();
    }

    @Override
    public void leaveChat(long chatId) {
        client.leaveChat(chatId);
    }

    private String upload(File file, UploadType uploadType, boolean useUploadedToken) {
        UploadEndpoint endpoint = client.getUploadUrl(uploadType);
        var info = client.uploadFile(endpoint.getUrl(), file);
        return useUploadedToken ? info.getToken() : endpoint.getToken();
    }

    private NewMessageBody toNewMessageBody(OutgoingMessage message) {
        List<AttachmentRequest> attachments = new ArrayList<>();
        if (message.keyboard() != null && !message.keyboard().isEmpty()) {
            attachments.add(new InlineKeyboardAttachmentRequest(
                    new InlineKeyboardAttachmentRequestPayload(message.keyboard())));
        }
        if (message.attachments() != null && !message.attachments().isEmpty()) {
            attachments.addAll(message.attachments());
        }

        NewMessageLink link = message.replyToMessageId() != null
                ? new NewMessageLink("reply", message.replyToMessageId())
                : null;

        NewMessageBody body = new NewMessageBody(
                message.text(),
                attachments.isEmpty() ? null : attachments,
                link
        );

        if (!message.shouldNotify()) {
            body.notify(false);
        }
        if (message.format() != null) {
            body.format(message.format() == OutgoingMessage.Format.MARKDOWN
                    ? "markdown"
                    : "html");
        }
        return body;
    }
}


