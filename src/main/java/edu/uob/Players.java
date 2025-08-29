package edu.uob;

import java.util.*;

public class Players extends GameEntity {

    public Locations currentLocation;
    public Map<String, Artefacts> playerInventory;
    public Integer playerHealth;
    public Players currentPlayer;

    public Players (String name, String description) {
        super(name, description);
        currentLocation = GameEntityParser.startingLocation;
        playerInventory = new HashMap<>();
        playerHealth = 3;
        this.currentPlayer = this;
    }
}
