package ru.maxbot.core;

import java.io.File;
import java.util.List;

import ru.maxbot.core.api.MaxApi;
import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.ChatMember;
import ru.maxbot.core.model.Update;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.OutgoingMessage;
import ru.maxbot.core.state.StateStore;

public final class UpdateContext {

    private final MaxApi api;
    private final Update update;
    private final StateStore stateStore;

    public UpdateContext(MaxApi api, Update update) {
        this(api, update, null);
    }

    public UpdateContext(MaxApi api, Update update, StateStore stateStore) {
        this.api = api;
        this.update = update;
        this.stateStore = stateStore;
    }

    public MaxApi api() {
        return api;
    }

    public Update update() {
        return update;
    }

    public long chatId() {
        return update.chatId();
    }

    public String messageId() {
        return update.messageId();
    }

    public String text() {
        return update.text();
    }

    public User sender() {
        return update.sender();
    }

    public String callbackData() {
        return update.callbackData();
    }

    public String callbackId() {
        return update.callbackId();
    }

    public String payload() {
        return update.payload();
    }

    // --- State management ---

    public String state() {
        return stateStore != null ? stateStore.getState(chatId()) : null;
    }

    public void setState(String state) {
        requireStateStore().setState(chatId(), state);
    }

    public void clearState() {
        requireStateStore().clearState(chatId());
    }

    // --- Reply ---

    public void reply(String text) {
        api.sendMessage(update.chatId(), text);
    }

    public void reply(OutgoingMessage message) {
        api.sendMessage(update.chatId(), message);
    }

    public void editMessage(String text) {
        api.editMessage(requireMessageId(), text);
    }

    public void editMessage(OutgoingMessage message) {
        api.editMessage(requireMessageId(), message);
    }

    public void deleteMessage() {
        api.deleteMessage(requireMessageId());
    }

    public void answerCallback(String notification) {
        api.answerCallback(requireCallbackId(), notification);
    }

    public void answerCallbackWithMessage(OutgoingMessage message) {
        api.answerCallback(requireCallbackId(), message);
    }

    // --- Chat ---

    public Chat getChat() {
        return api.getChat(update.chatId());
    }

    public List<ChatMember> getChatMembers() {
        return api.getChatMembers(update.chatId());
    }

    public void leaveChat() {
        api.leaveChat(update.chatId());
    }

    // --- Upload ---

    public String uploadImage(File file) {
        return api.uploadImage(file);
    }

    public String uploadVideo(File file) {
        return api.uploadVideo(file);
    }

    public String uploadAudio(File file) {
        return api.uploadAudio(file);
    }

    public String uploadFile(File file) {
        return api.uploadFile(file);
    }

    public void replyWithImage(String text, File imageFile) {
        String token = api.uploadImage(imageFile);
        reply(OutgoingMessage.text(text)
                .attach(OutgoingMessage.photo(token))
                .build());
    }

    // --- Private ---

    private StateStore requireStateStore() {
        if (stateStore == null) {
            throw new IllegalStateException(
                    "StateStore not configured. Register a StateStore bean to use state management.");
        }
        return stateStore;
    }

    private String requireMessageId() {
        String mid = update.messageId();
        if (mid == null) {
            throw new IllegalStateException("messageId is null for update type=" + update.type());
        }
        return mid;
    }

    private String requireCallbackId() {
        String cbId = update.callbackId();
        if (cbId == null) {
            throw new IllegalStateException("callbackId is null for update type=" + update.type());
        }
        return cbId;
    }
}


