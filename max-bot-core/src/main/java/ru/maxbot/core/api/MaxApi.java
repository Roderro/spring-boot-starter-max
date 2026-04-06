package ru.maxbot.core.api;

import java.io.File;
import java.util.List;

import ru.maxbot.core.model.Chat;
import ru.maxbot.core.model.ChatMember;
import ru.maxbot.core.model.User;
import ru.maxbot.core.outgoing.OutgoingMessage;

public interface MaxApi {

    void sendMessage(long chatId, String text);

    void sendMessage(long chatId, OutgoingMessage message);

    void editMessage(String messageId, String text);

    void editMessage(String messageId, OutgoingMessage message);

    void deleteMessage(String messageId);

    void answerCallback(String callbackId, String notification);

    void answerCallback(String callbackId, OutgoingMessage message);

    User getMe();

    String uploadImage(File file);

    String uploadVideo(File file);

    String uploadAudio(File file);

    String uploadFile(File file);

    Chat getChat(long chatId);

    List<ChatMember> getChatMembers(long chatId);

    void leaveChat(long chatId);
}


