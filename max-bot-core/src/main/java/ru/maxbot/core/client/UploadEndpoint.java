package ru.maxbot.core.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadEndpoint {

    private final String url;
    private String token;

    public UploadEndpoint(@JsonProperty("url") String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public UploadEndpoint token(String token) {
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

