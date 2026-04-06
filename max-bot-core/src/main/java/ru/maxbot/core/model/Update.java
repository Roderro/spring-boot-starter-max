package ru.maxbot.core.model;

public record Update(
        UpdateType type,
        long chatId,
        String messageId,
        String text,
        String callbackData,
        String callbackId,
        User sender,
        long timestamp,
        String userLocale,
        String payload
) {

    public static Update ofMessage(long chatId, String messageId, String text,
                                   User sender, long timestamp, String userLocale) {
        return new Update(UpdateType.MESSAGE_CREATED, chatId, messageId, text,
                null, null, sender, timestamp, userLocale, null);
    }

    public static Update ofCallback(long chatId, String messageId, String text,
                                    String callbackData, String callbackId,
                                    User sender, long timestamp, String userLocale) {
        return new Update(UpdateType.MESSAGE_CALLBACK, chatId, messageId, text,
                callbackData, callbackId, sender, timestamp, userLocale, null);
    }

    public static Update ofBotStarted(long chatId, User user, long timestamp,
                                      String payload, String userLocale) {
        return new Update(UpdateType.BOT_STARTED, chatId, null, null,
                null, null, user, timestamp, userLocale, payload);
    }

    public static Update ofMessageEdited(long chatId, String messageId, String text,
                                         User sender, long timestamp) {
        return new Update(UpdateType.MESSAGE_EDITED, chatId, messageId, text,
                null, null, sender, timestamp, null, null);
    }

    public static Update ofMessageRemoved(long chatId, String messageId, long timestamp) {
        return new Update(UpdateType.MESSAGE_REMOVED, chatId, messageId, null,
                null, null, null, timestamp, null, null);
    }

    public static Update ofBotAdded(long chatId, User user, long timestamp) {
        return new Update(UpdateType.BOT_ADDED, chatId, null, null,
                null, null, user, timestamp, null, null);
    }

    public static Update ofBotRemoved(long chatId, User user, long timestamp) {
        return new Update(UpdateType.BOT_REMOVED, chatId, null, null,
                null, null, user, timestamp, null, null);
    }

    public static Update ofUserAdded(long chatId, User user, long timestamp) {
        return new Update(UpdateType.USER_ADDED, chatId, null, null,
                null, null, user, timestamp, null, null);
    }

    public static Update ofUserRemoved(long chatId, User user, long timestamp) {
        return new Update(UpdateType.USER_REMOVED, chatId, null, null,
                null, null, user, timestamp, null, null);
    }

    public static Update ofChatTitleChanged(long chatId, String newTitle,
                                            User user, long timestamp) {
        return new Update(UpdateType.CHAT_TITLE_CHANGED, chatId, null, newTitle,
                null, null, user, timestamp, null, null);
    }

    public static Update ofMessageChatCreated(long chatId, String messageId,
                                              long timestamp, String startPayload) {
        return new Update(UpdateType.MESSAGE_CHAT_CREATED, chatId, messageId, null,
                null, null, null, timestamp, null, startPayload);
    }

    public static Update of(long chatId, String text, String callbackData) {
        UpdateType type = callbackData != null
                ? UpdateType.MESSAGE_CALLBACK
                : UpdateType.MESSAGE_CREATED;
        return new Update(type, chatId, null, text, callbackData, null, null, 0L, null, null);
    }
}


