package com.yams.model;

import java.util.Map;

public class GameEvent {
    private String type;
    private Long gameId;
    private Map<String, Object> data;

    public GameEvent() {}

    public GameEvent(String type, Long gameId, Map<String, Object> data) {
        this.type = type;
        this.gameId = gameId;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
