package ru.maxbot.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ChatStatus {
    ACTIVE("active"),
    REMOVED("removed"),
    LEFT("left"),
    CLOSED("closed"),
    SUSPENDED("suspended");

    private final String value;

    ChatStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @JsonCreator
    public static ChatStatus create(String value) {
        for (ChatStatus status : values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        return null;
    }
}

