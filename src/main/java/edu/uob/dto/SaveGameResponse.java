package edu.uob.dto;

import java.time.LocalDateTime;

public class SaveGameResponse {
    private String gameId;
    private String saveSlotName;
    private String status;
    private LocalDateTime timestamp;

    public SaveGameResponse() {}

    public SaveGameResponse(String gameId, String saveSlotName, String status, LocalDateTime timestamp) {
        this.gameId = gameId;
        this.saveSlotName = saveSlotName;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getSaveSlotName() {
        return saveSlotName;
    }

    public void setSaveSlotName(String saveSlotName) {
        this.saveSlotName = saveSlotName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
