package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadedInfo {

    private String token;

    public UploadedInfo() {
    }

    public UploadedInfo(@JsonProperty("token") String token) {
        this.token = token;
    }

    public UploadedInfo token(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}

