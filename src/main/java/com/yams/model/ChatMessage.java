package com.yams.model;

public class ChatMessage {
    private String from;
    private String content;
    private Long gameId;

    public ChatMessage() {}

    public ChatMessage(String from, String content, Long gameId) {
        this.from = from;
        this.content = content;
        this.gameId = gameId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getGameId() {
        return gameId;
    }

    public void setGameId(Long gameId) {
        this.gameId = gameId;
    }
}
