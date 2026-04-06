package ru.maxbot.core.client;

public class CallbackAnswer {

    private NewMessageBody message;
    private String notification;

    public CallbackAnswer message(NewMessageBody message) {
        this.message = message;
        return this;
    }

    public NewMessageBody getMessage() {
        return message;
    }

    public void setMessage(NewMessageBody message) {
        this.message = message;
    }

    public CallbackAnswer notification(String notification) {
        this.notification = notification;
        return this;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }
}

