package ru.maxbot.core.mapper;

import ru.maxbot.core.model.Update;
import ru.maxbot.core.model.UpdateType;
import ru.maxbot.core.transport.IncomingCallback;
import ru.maxbot.core.transport.IncomingMessage;
import ru.maxbot.core.transport.IncomingRecipient;
import ru.maxbot.core.transport.IncomingUpdate;

public final class UpdateMapper {

    public Update map(IncomingUpdate apiUpdate) {
        return switch (apiUpdate.getUpdateType()) {
            case "message_created" -> {
                IncomingMessage msg = apiUpdate.getMessage();
                yield Update.ofMessage(
                        extractChatId(msg),
                        extractMid(msg),
                        extractText(msg),
                        msg != null ? msg.getSender() : null,
                        nullSafeTimestamp(apiUpdate.getTimestamp()),
                        apiUpdate.getUserLocale()
                );
            }
            case "message_callback" -> {
                IncomingCallback cb = apiUpdate.getCallback();
                IncomingMessage msg = apiUpdate.getMessage();
                yield Update.ofCallback(
                        msg != null ? extractChatId(msg) : 0L,
                        msg != null ? extractMid(msg) : null,
                        msg != null ? extractText(msg) : null,
                        cb != null ? cb.getPayload() : null,
                        cb != null ? cb.getCallbackId() : null,
                        cb != null ? cb.getUser() : null,
                        nullSafeTimestamp(apiUpdate.getTimestamp()),
                        apiUpdate.getUserLocale()
                );
            }
            case "bot_started" -> Update.ofBotStarted(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp()),
                    apiUpdate.getPayload(),
                    apiUpdate.getUserLocale()
            );
            case "message_edited" -> {
                IncomingMessage msg = apiUpdate.getMessage();
                yield Update.ofMessageEdited(
                        extractChatId(msg),
                        extractMid(msg),
                        extractText(msg),
                        msg != null ? msg.getSender() : null,
                        nullSafeTimestamp(apiUpdate.getTimestamp())
                );
            }
            case "message_removed" -> Update.ofMessageRemoved(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getMessageId(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "bot_added" -> Update.ofBotAdded(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "bot_removed" -> Update.ofBotRemoved(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "user_added" -> Update.ofUserAdded(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "user_removed" -> Update.ofUserRemoved(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "chat_title_changed" -> Update.ofChatTitleChanged(
                    nullSafeLong(apiUpdate.getChatId()),
                    apiUpdate.getTitle(),
                    apiUpdate.getUser(),
                    nullSafeTimestamp(apiUpdate.getTimestamp())
            );
            case "message_chat_created" -> Update.ofMessageChatCreated(
                    apiUpdate.getChat() != null ? nullSafeLong(apiUpdate.getChat().getChatId()) : 0L,
                    apiUpdate.getMessageId(),
                    nullSafeTimestamp(apiUpdate.getTimestamp()),
                    apiUpdate.getStartPayload()
            );
            default -> new Update(UpdateType.UNKNOWN, 0L, null, null,
                    null, null, null, nullSafeTimestamp(apiUpdate.getTimestamp()), null, null);
        };
    }

    private static long extractChatId(IncomingMessage msg) {
        if (msg == null || msg.getRecipient() == null) {
            return 0L;
        }
        IncomingRecipient recipient = msg.getRecipient();
        if (recipient.getChatId() != null) {
            return recipient.getChatId();
        }
        if (recipient.getUserId() != null) {
            return recipient.getUserId();
        }
        return 0L;
    }

    private static String extractMid(IncomingMessage msg) {
        if (msg == null || msg.getBody() == null) {
            return null;
        }
        return msg.getBody().getMid();
    }

    private static String extractText(IncomingMessage msg) {
        if (msg == null || msg.getBody() == null) {
            return null;
        }
        return msg.getBody().getText();
    }

    private static long nullSafeTimestamp(Long value) {
        return value != null ? value : 0L;
    }

    private static long nullSafeLong(Long value) {
        return value != null ? value : 0L;
    }
}


