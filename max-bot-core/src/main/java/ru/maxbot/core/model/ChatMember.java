package ru.maxbot.core.model;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatMember extends User {

    @JsonProperty("last_access_time")
    private final Long lastAccessTime;
    @JsonProperty("is_owner")
    private final Boolean owner;
    @JsonProperty("is_admin")
    private final Boolean admin;
    @JsonProperty("join_time")
    private final Long joinTime;
    private final Set<String> permissions;

    public ChatMember(@JsonProperty("last_access_time") Long lastAccessTime,
                      @JsonProperty("is_owner") Boolean owner,
                      @JsonProperty("is_admin") Boolean admin,
                      @JsonProperty("join_time") Long joinTime,
                      @JsonProperty("permissions") Set<String> permissions,
                      @JsonProperty("user_id") Long userId,
                      @JsonProperty("first_name") String firstName,
                      @JsonProperty("last_name") String lastName,
                      @JsonProperty("username") String username,
                      @JsonProperty("is_bot") Boolean bot,
                      @JsonProperty("last_activity_time") Long lastActivityTime) {
        super(userId, firstName, lastName, username, bot, lastActivityTime);
        this.lastAccessTime = lastAccessTime;
        this.owner = owner;
        this.admin = admin;
        this.joinTime = joinTime;
        this.permissions = permissions;
    }

    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    public Boolean isOwner() {
        return owner;
    }

    public Boolean isAdmin() {
        return admin;
    }

    public Long getJoinTime() {
        return joinTime;
    }

    public Set<String> getPermissions() {
        return permissions;
    }
}

