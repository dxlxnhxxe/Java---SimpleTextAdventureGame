package edu.uob.dto;

import java.util.List;

public class CommandResponse {
    private String playerName;
    private String gameId;
    private String narrative;
    private String currentLocation;
    private int health;
    private List<String> inventory;
    private List<String> otherPlayersInLocation;
    private List<String> availablePaths;
    private List<String> availableArtefacts;
    private List<String> availableFurniture;
    private List<String> availableExtendedCommands;

    public CommandResponse() {}

    public CommandResponse(String playerName, String gameId, String narrative, String currentLocation,
                           int health, List<String> inventory, List<String> otherPlayersInLocation) {
        this(playerName, gameId, narrative, currentLocation, health, inventory, otherPlayersInLocation, null, null, null, null);
    }

    public CommandResponse(String playerName, String gameId, String narrative, String currentLocation,
                           int health, List<String> inventory, List<String> otherPlayersInLocation,
                           List<String> availablePaths, List<String> availableArtefacts, List<String> availableFurniture) {
        this(playerName, gameId, narrative, currentLocation, health, inventory, otherPlayersInLocation, availablePaths, availableArtefacts, availableFurniture, null);
    }

    public CommandResponse(String playerName, String gameId, String narrative, String currentLocation,
                           int health, List<String> inventory, List<String> otherPlayersInLocation,
                           List<String> availablePaths, List<String> availableArtefacts, List<String> availableFurniture,
                           List<String> availableExtendedCommands) {
        this.playerName = playerName;
        this.gameId = gameId;
        this.narrative = narrative;
        this.currentLocation = currentLocation;
        this.health = health;
        this.inventory = inventory;
        this.otherPlayersInLocation = otherPlayersInLocation;
        this.availablePaths = availablePaths;
        this.availableArtefacts = availableArtefacts;
        this.availableFurniture = availableFurniture;
        this.availableExtendedCommands = availableExtendedCommands;
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

    public String getNarrative() {
        return narrative;
    }

    public void setNarrative(String narrative) {
        this.narrative = narrative;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
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

    public List<String> getOtherPlayersInLocation() {
        return otherPlayersInLocation;
    }

    public void setOtherPlayersInLocation(List<String> otherPlayersInLocation) {
        this.otherPlayersInLocation = otherPlayersInLocation;
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
