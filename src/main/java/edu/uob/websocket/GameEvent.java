package edu.uob.websocket;

import java.time.LocalDateTime;

public class GameEvent {

    public enum EventType {
        PLAYER_JOINED,
        PLAYER_LEFT,
        PLAYER_MOVED,
        PLAYER_ACTION,
        ITEM_DROPPED,
        ITEM_PICKED_UP,
        GLOBAL_ANNOUNCEMENT,
        CHAT_MESSAGE
    }

    private EventType eventType;
    private String gameId;
    private String player;
    private String location;
    private String message;
    private LocalDateTime timestamp;

    public GameEvent() {
        this.timestamp = LocalDateTime.now();
    }

    public GameEvent(EventType eventType, String gameId, String player, String location, String message) {
        this.eventType = eventType;
        this.gameId = gameId;
        this.player = player;
        this.location = location;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
