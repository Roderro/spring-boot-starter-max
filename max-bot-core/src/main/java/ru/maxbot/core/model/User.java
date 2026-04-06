package ru.maxbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    @JsonProperty("user_id")
    private final Long userId;
    @JsonProperty("first_name")
    private final String firstName;
    @JsonProperty("last_name")
    private final String lastName;
    private final String username;
    @JsonProperty("is_bot")
    private final Boolean bot;
    @JsonProperty("last_activity_time")
    private final Long lastActivityTime;
    private String name;

    public User(@JsonProperty("user_id") Long userId,
                @JsonProperty("first_name") String firstName,
                @JsonProperty("last_name") String lastName,
                @JsonProperty("username") String username,
                @JsonProperty("is_bot") Boolean bot,
                @JsonProperty("last_activity_time") Long lastActivityTime) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.bot = bot;
        this.lastActivityTime = lastActivityTime;
    }

    public Long getUserId() {
        return userId;
    }

    public User name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public Boolean isBot() {
        return bot;
    }

    public Long getLastActivityTime() {
        return lastActivityTime;
    }
}

