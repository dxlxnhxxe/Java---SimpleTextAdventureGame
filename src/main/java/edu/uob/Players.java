package edu.uob;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Players extends GameEntity {

    public Locations currentLocation;
    public Map<String, Artefacts> playerInventory;
    public Integer playerHealth;
    public Players currentPlayer;
    private GameWorld gameWorld;

    public Players(String name, String description) {
        this(name, description, GameEntityParser.startingLocation, null);
    }

    public Players(String name, String description, Locations startingLocation, GameWorld gameWorld) {
        super(name, description);
        this.currentLocation = startingLocation;
        this.playerInventory = new ConcurrentHashMap<>();
        this.playerHealth = 3;
        this.currentPlayer = this;
        this.gameWorld = gameWorld;
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public void setGameWorld(GameWorld gameWorld) {
        this.gameWorld = gameWorld;
    }

    public Locations getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Locations currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Map<String, Artefacts> getPlayerInventory() {
        return playerInventory;
    }

    public Integer getPlayerHealth() {
        return playerHealth;
    }

    public void setPlayerHealth(Integer playerHealth) {
        this.playerHealth = playerHealth;
    }
}
