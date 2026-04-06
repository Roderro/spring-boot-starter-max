package ru.maxbot.core.client;

public enum UploadType {
    IMAGE("image"),
    VIDEO("video"),
    AUDIO("audio"),
    FILE("file");

    private final String value;

    UploadType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

