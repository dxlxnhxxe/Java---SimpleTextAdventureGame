package edu.uob.dto;

import java.util.List;

public class GameSessionResponse {
    private String gameId;
    private String gameName;
    private String startingLocation;
    private int playerCount;
    private List<String> players;

    public GameSessionResponse() {}

    public GameSessionResponse(String gameId, String gameName, String startingLocation, int playerCount, List<String> players) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.startingLocation = startingLocation;
        this.playerCount = playerCount;
        this.players = players;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        this.playerCount = playerCount;
    }

    public List<String> getPlayers() {
        return players;
    }

    public void setPlayers(List<String> players) {
        this.players = players;
    }
}
