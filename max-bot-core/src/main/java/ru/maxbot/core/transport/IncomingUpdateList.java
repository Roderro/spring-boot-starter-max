package ru.maxbot.core.transport;

import java.util.List;

public class IncomingUpdateList {

    private List<IncomingUpdate> updates;
    private Long marker;

    public List<IncomingUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<IncomingUpdate> updates) {
        this.updates = updates;
    }

    public Long getMarker() {
        return marker;
    }

    public void setMarker(Long marker) {
        this.marker = marker;
    }
}

