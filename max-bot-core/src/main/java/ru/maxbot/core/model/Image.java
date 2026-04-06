package ru.maxbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Image {

    private final String url;
    @JsonProperty("width")
    private final Integer width;
    @JsonProperty("height")
    private final Integer height;

    public Image(@JsonProperty("url") String url,
                 @JsonProperty("width") Integer width,
                 @JsonProperty("height") Integer height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }
}

