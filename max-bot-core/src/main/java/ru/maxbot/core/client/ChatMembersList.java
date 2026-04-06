package ru.maxbot.core.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.maxbot.core.model.ChatMember;

public class ChatMembersList {

    private List<ChatMember> members;
    private Long marker;
    private Integer count;

    public List<ChatMember> getMembers() {
        return members;
    }

    public void setMembers(List<ChatMember> members) {
        this.members = members;
    }

    public Long getMarker() {
        return marker;
    }

    public void setMarker(Long marker) {
        this.marker = marker;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}

