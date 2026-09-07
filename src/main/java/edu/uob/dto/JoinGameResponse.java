package edu.uob.dto;

import java.util.List;

public class JoinGameResponse {
    private String playerId;
    private String playerName;
    private String gameId;
    private String currentLocation;
    private String locationDescription;
    private int health;
    private List<String> inventory;
    private List<String> availablePaths;
    private List<String> availableArtefacts;
    private List<String> availableFurniture;
    private List<String> availableExtendedCommands;

    public JoinGameResponse() {}

    public JoinGameResponse(String playerId, String playerName, String gameId, String currentLocation,
                            String locationDescription, int health, List<String> inventory) {
        this(playerId, playerName, gameId, currentLocation, locationDescription, health, inventory, null, null, null, null);
    }

    public JoinGameResponse(String playerId, String playerName, String gameId, String currentLocation,
                            String locationDescription, int health, List<String> inventory,
                            List<String> availablePaths, List<String> availableArtefacts, List<String> availableFurniture) {
        this(playerId, playerName, gameId, currentLocation, locationDescription, health, inventory, availablePaths, availableArtefacts, availableFurniture, null);
    }

    public JoinGameResponse(String playerId, String playerName, String gameId, String currentLocation,
                            String locationDescription, int health, List<String> inventory,
                            List<String> availablePaths, List<String> availableArtefacts, List<String> availableFurniture,
                            List<String> availableExtendedCommands) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.gameId = gameId;
        this.currentLocation = currentLocation;
        this.locationDescription = locationDescription;
        this.health = health;
        this.inventory = inventory;
        this.availablePaths = availablePaths;
        this.availableArtefacts = availableArtefacts;
        this.availableFurniture = availableFurniture;
        this.availableExtendedCommands = availableExtendedCommands;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public void setLocationDescription(String locationDescription) {
        this.locationDescription = locationDescription;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public List<String> getInventory() {
        return inventory;
    }

    public void setInventory(List<String> inventory) {
        this.inventory = inventory;
    }

    public List<String> getAvailablePaths() {
        return availablePaths;
    }

    public void setAvailablePaths(List<String> availablePaths) {
        this.availablePaths = availablePaths;
    }

    public List<String> getAvailableArtefacts() {
        return availableArtefacts;
    }

    public void setAvailableArtefacts(List<String> availableArtefacts) {
        this.availableArtefacts = availableArtefacts;
    }

    public List<String> getAvailableFurniture() {
        return availableFurniture;
    }

    public void setAvailableFurniture(List<String> availableFurniture) {
        this.availableFurniture = availableFurniture;
    }

    public List<String> getAvailableExtendedCommands() {
        return availableExtendedCommands;
    }

    public void setAvailableExtendedCommands(List<String> availableExtendedCommands) {
        this.availableExtendedCommands = availableExtendedCommands;
    }
}
