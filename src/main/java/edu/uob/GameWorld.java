package edu.uob;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents an active instance of a STAG game world.
 * All locations, entities, players, and action rules are contained within this instance,
 * enabling multiple isolated concurrent game sessions.
 */
public class GameWorld {

    private final String worldId;
    private String worldName;

    private Locations startingLocation;
    private final Map<String, Locations> allLocations = new LinkedHashMap<>();
    private final Map<String, LinkedList<String>> locationPaths = new LinkedHashMap<>();

    private final Map<String, GameEntity> allEntities = new LinkedHashMap<>();
    private final Map<String, Map<String, Players>> locationWithPlayers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Furnitures>> locationWithFurnitures = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Artefacts>> locationWithArtefacts = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Characters>> locationWithCharacters = new ConcurrentHashMap<>();

    private final Set<String> artefactNames = ConcurrentHashMap.newKeySet();
    private final Set<String> furnitureNames = ConcurrentHashMap.newKeySet();
    private final Set<String> characterNames = ConcurrentHashMap.newKeySet();

    private final List<GameActionNode> actions = new CopyOnWriteArrayList<>();
    private final Set<String> extendedCommands = ConcurrentHashMap.newKeySet();
    private final Map<String, String> extendedKeyphraseSynonyms = new ConcurrentHashMap<>();
    private final Map<String, String> allCommandSynonyms = new ConcurrentHashMap<>();

    private final Map<String, Players> allPlayers = new ConcurrentHashMap<>();

    public GameWorld() {
        this(UUID.randomUUID().toString(), "STAG Adventure");
    }

    public GameWorld(String worldId, String worldName) {
        this.worldId = worldId;
        this.worldName = worldName;
        this.allCommandSynonyms.put("inv", "inventory");
        this.allCommandSynonyms.put("inventory", "inventory");
    }

    public static GameWorld createFromFiles(File entitiesFile, File actionsFile) {
        GameWorld world = new GameWorld();
        GameEntityParser.parseEntitiesToWorld(entitiesFile, world);
        GameActionParser.parseXMLToWorld(actionsFile, world);
        world.allCommandSynonyms.putAll(world.getExtendedKeyphraseSynonyms());
        return world;
    }

    public String getWorldId() {
        return worldId;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public Locations getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(Locations startingLocation) {
        this.startingLocation = startingLocation;
    }

    public Map<String, Locations> getAllLocations() {
        return allLocations;
    }

    public Map<String, LinkedList<String>> getLocationPaths() {
        return locationPaths;
    }

    public Map<String, GameEntity> getAllEntities() {
        return allEntities;
    }

    public Map<String, Map<String, Players>> getLocationWithPlayers() {
        return locationWithPlayers;
    }

    public Map<String, Map<String, Furnitures>> getLocationWithFurnitures() {
        return locationWithFurnitures;
    }

    public Map<String, Map<String, Artefacts>> getLocationWithArtefacts() {
        return locationWithArtefacts;
    }

    public Map<String, Map<String, Characters>> getLocationWithCharacters() {
        return locationWithCharacters;
    }

    public Set<String> getArtefactNames() {
        return artefactNames;
    }

    public Set<String> getFurnitureNames() {
        return furnitureNames;
    }

    public Set<String> getCharacterNames() {
        return characterNames;
    }

    public List<GameActionNode> getActions() {
        return actions;
    }

    public Set<String> getExtendedCommands() {
        return extendedCommands;
    }

    public Map<String, String> getExtendedKeyphraseSynonyms() {
        return extendedKeyphraseSynonyms;
    }

    public Map<String, String> getAllCommandSynonyms() {
        return allCommandSynonyms;
    }

    public Map<String, Players> getAllPlayers() {
        return allPlayers;
    }

    public Players getOrCreatePlayer(String username) {
        return allPlayers.computeIfAbsent(username, name -> {
            Players player = new Players(name, "A player in the realm", startingLocation, this);
            if (startingLocation != null) {
                Map<String, Players> playersInLoc = locationWithPlayers.computeIfAbsent(
                        startingLocation.getName(), k -> new ConcurrentHashMap<>());
                playersInLoc.put(name, player);
            }
            return player;
        });
    }

    public synchronized String handleCommand(String command) {
        if (command == null) {
            return "ERROR: Command cannot be null.";
        }
        int colonIndex = command.indexOf(':');
        if (colonIndex == -1) {
            return "ERROR: Invalid command format. Missing ':' between username and command.";
        }
        String username = command.substring(0, colonIndex).trim();
        if (!username.matches("[a-zA-Z\\s'-]+")) {
            return "ERROR: Invalid username format.";
        }

        Players currentPlayer = getOrCreatePlayer(username);

        String userCommand = command.substring(colonIndex + 1).trim().replaceAll("\\s+", " ").toLowerCase();
        if (userCommand.isEmpty()) {
            return "ERROR: Empty command after username.";
        }

        Set<String> keyphrases = CommandTokeniser.buildExtendedKeyphraseSet(actions);
        CommandTokeniser.TokenisedCommand tokenised = CommandTokeniser.tokeniseAndClassifyCommand(
                userCommand, keyphrases, ExecuteBasicCommands.basicCommands, allCommandSynonyms, currentPlayer, this);

        CommandTokeniser.CommandCheckResult result = tokenised.result;
        switch (result) {
            case BASIC_COMMAND:
                return ExecuteBasicCommands.executeBasicCommand(this, currentPlayer, userCommand);
            case EXTENDED_COMMAND:
                return ExecuteExtendedCommands.executeExtendedCommand(this, userCommand, currentPlayer);
            case AMBIGUOUS_COMMAND:
                return "ERROR: Ambiguous command.";
            case NO_COMMAND_FOUND:
                return userCommand;
            default:
                return "ERROR: Unhandled command scenario.";
        }
    }
}
