package ru.maxbot.core.outgoing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CallbackButton.class, name = Button.CALLBACK),
        @JsonSubTypes.Type(value = LinkButton.class, name = Button.LINK)
})
public abstract class Button {

    public static final String CALLBACK = "callback";
    public static final String LINK = "link";

    private final String text;
    private final String type;

    protected Button(String text, String type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }
}

