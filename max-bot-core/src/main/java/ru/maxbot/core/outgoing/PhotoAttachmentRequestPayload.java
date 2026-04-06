package ru.maxbot.core.outgoing;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PhotoAttachmentRequestPayload {

    private String url;
    private String token;
    private Map<String, UploadedInfo> photos;

    public PhotoAttachmentRequestPayload url(String url) {
        this.url = url;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public PhotoAttachmentRequestPayload token(String token) {
        this.token = token;
        return this;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public PhotoAttachmentRequestPayload photos(Map<String, UploadedInfo> photos) {
        this.photos = photos;
        return this;
    }

    @JsonProperty("photos")
    public Map<String, UploadedInfo> getPhotos() {
        return photos;
    }

    public void setPhotos(Map<String, UploadedInfo> photos) {
        this.photos = photos;
    }
}

