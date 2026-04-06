package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;

public final class LinkButton extends Button {

    private final String url;

    public LinkButton(@JsonProperty("url") String url,
                      @JsonProperty("text") String text) {
        super(text, LINK);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}

