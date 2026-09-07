package edu.uob.service;

import edu.uob.GameWorld;
import edu.uob.GameWorldTemplate;
import edu.uob.Locations;
import edu.uob.Players;
import edu.uob.dto.*;
import edu.uob.websocket.GameEvent;
import edu.uob.websocket.GameEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class GameEngineService {

    @Value("${stag.config.entities-path:config/extended-entities.dot}")
    private String entitiesPath;

    @Value("${stag.config.actions-path:config/extended-actions.xml}")
    private String actionsPath;

    private final Map<String, GameWorld> activeWorlds = new ConcurrentHashMap<>();
    private GameWorldTemplate defaultTemplate;
    private final GameEventPublisher eventPublisher;

    public GameEngineService(@Lazy @Autowired(required = false) GameEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        File entitiesFile = Paths.get(entitiesPath).toAbsolutePath().toFile();
        File actionsFile = Paths.get(actionsPath).toAbsolutePath().toFile();
        this.defaultTemplate = new GameWorldTemplate(entitiesFile, actionsFile);
    }

    public GameWorldTemplate getTemplate() {
        return defaultTemplate;
    }

    public GameSessionResponse createGame(String gameName, String templateType) {
        String name = (gameName != null && !gameName.isBlank()) ? gameName : "Adventure Realm";
        String gameId = UUID.randomUUID().toString().substring(0, 8);
        GameWorld world = defaultTemplate.createNewWorld(gameId, name);
        activeWorlds.put(gameId, world);

        String startingLoc = (world.getStartingLocation() != null)
                ? world.getStartingLocation().getName()
                : "unknown";

        return new GameSessionResponse(gameId, name, startingLoc, 0, Collections.emptyList());
    }

    public List<GameSessionResponse> listGames() {
        return activeWorlds.values().stream()
                .map(this::toGameSessionResponse)
                .collect(Collectors.toList());
    }

    public GameSessionResponse getGame(String gameId) {
        GameWorld world = getGameWorld(gameId);
        return toGameSessionResponse(world);
    }

    public GameWorld getGameWorld(String gameId) {
        GameWorld world = activeWorlds.get(gameId);
        if (world == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game session not found with ID: " + gameId);
        }
        return world;
    }

    public void registerGameWorld(GameWorld world) {
        activeWorlds.put(world.getWorldId(), world);
    }

    public JoinGameResponse joinGame(String gameId, String playerName) {
        if (playerName == null || playerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player name cannot be blank");
        }
        GameWorld world = getGameWorld(gameId);
        Players player = world.getOrCreatePlayer(playerName.trim());

        Locations loc = player.getCurrentLocation();
        String locName = (loc != null) ? loc.getName() : "unknown";
        String locDesc = (loc != null) ? loc.getDescription() : "";

        List<String> inv = (player.getPlayerInventory() != null)
                ? new ArrayList<>(player.getPlayerInventory().keySet())
                : Collections.emptyList();

        if (eventPublisher != null) {
            GameEvent event = new GameEvent(
                    GameEvent.EventType.PLAYER_JOINED,
                    gameId,
                    player.getName(),
                    locName,
                    String.format("%s joined the adventure in the %s.", player.getName(), locName)
            );
            eventPublisher.publishLocationEvent(gameId, locName, event);
        }

        List<String> paths = getAvailablePaths(world, locName);
        List<String> artefacts = getAvailableArtefacts(world, locName);
        List<String> furnitures = getAvailableFurniture(world, locName);
        List<String> extendedCommands = getAvailableExtendedCommands(world, player);

        return new JoinGameResponse(
                UUID.randomUUID().toString(),
                player.getName(),
                gameId,
                locName,
                locDesc,
                player.getPlayerHealth(),
                inv,
                paths,
                artefacts,
                furnitures,
                extendedCommands
        );
    }

    public CommandResponse executeCommand(String gameId, String playerName, String command) {
        if (playerName == null || playerName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Player name cannot be blank");
        }
        if (command == null || command.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Command cannot be blank");
        }

        GameWorld world = getGameWorld(gameId);
        Players player = world.getOrCreatePlayer(playerName.trim());
        String oldLoc = (player.getCurrentLocation() != null) ? player.getCurrentLocation().getName() : "unknown";

        String rawResult = world.handleCommand(playerName.trim() + ": " + command.trim());

        Locations loc = player.getCurrentLocation();
        String newLoc = (loc != null) ? loc.getName() : "unknown";

        List<String> inv = (player.getPlayerInventory() != null)
                ? new ArrayList<>(player.getPlayerInventory().keySet())
                : Collections.emptyList();

        List<String> otherPlayers = new ArrayList<>();
        Map<String, Players> playersHere = world.getLocationWithPlayers().get(newLoc);
        if (playersHere != null) {
            for (Players p : playersHere.values()) {
                if (!p.getName().equalsIgnoreCase(playerName.trim())) {
                    otherPlayers.add(p.getName());
                }
            }
        }

        // Publish STOMP notifications if location changed or action occurred
        if (eventPublisher != null) {
            if (!oldLoc.equalsIgnoreCase(newLoc)) {
                eventPublisher.publishLocationEvent(gameId, oldLoc, new GameEvent(
                        GameEvent.EventType.PLAYER_MOVED, gameId, player.getName(), oldLoc,
                        String.format("%s left for the %s.", player.getName(), newLoc)
                ));
                eventPublisher.publishLocationEvent(gameId, newLoc, new GameEvent(
                        GameEvent.EventType.PLAYER_MOVED, gameId, player.getName(), newLoc,
                        String.format("%s arrived from the %s.", player.getName(), oldLoc)
                ));
            } else if (!command.trim().equalsIgnoreCase("look") && !command.trim().equalsIgnoreCase("inventory") && !command.trim().equalsIgnoreCase("health")) {
                eventPublisher.publishLocationEvent(gameId, newLoc, new GameEvent(
                        GameEvent.EventType.PLAYER_ACTION, gameId, player.getName(), newLoc,
                        String.format("%s performed an action: '%s'.", player.getName(), command.trim())
                ));
            }
        }

        List<String> paths = getAvailablePaths(world, newLoc);
        List<String> artefacts = getAvailableArtefacts(world, newLoc);
        List<String> furnitures = getAvailableFurniture(world, newLoc);
        List<String> extendedCommands = getAvailableExtendedCommands(world, player);

        return new CommandResponse(
                player.getName(),
                gameId,
                rawResult,
                newLoc,
                player.getPlayerHealth(),
                inv,
                otherPlayers,
                paths,
                artefacts,
                furnitures,
                extendedCommands
        );
    }

    private List<String> getAvailablePaths(GameWorld world, String locationName) {
        if (world == null || locationName == null) return Collections.emptyList();
        LinkedList<String> paths = world.getLocationPaths().get(locationName.toLowerCase());
        return paths != null ? new ArrayList<>(paths) : Collections.emptyList();
    }

    private List<String> getAvailableArtefacts(GameWorld world, String locationName) {
        if (world == null || locationName == null) return Collections.emptyList();
        Map<String, edu.uob.Artefacts> arts = world.getLocationWithArtefacts().get(locationName.toLowerCase());
        return arts != null ? new ArrayList<>(arts.keySet()) : Collections.emptyList();
    }

    private List<String> getAvailableFurniture(GameWorld world, String locationName) {
        if (world == null || locationName == null) return Collections.emptyList();
        Map<String, edu.uob.Furnitures> furn = world.getLocationWithFurnitures().get(locationName.toLowerCase());
        return furn != null ? new ArrayList<>(furn.keySet()) : Collections.emptyList();
    }

    private List<String> getAvailableExtendedCommands(GameWorld world, Players player) {
        if (world == null || player == null || player.getCurrentLocation() == null) {
            return Collections.emptyList();
        }
        List<edu.uob.GameActionNode> actions = world.getActions();
        if (actions == null) return Collections.emptyList();

        List<String> possibleCommands = new ArrayList<>();
        Set<String> seenCommands = new HashSet<>();

        for (edu.uob.GameActionNode action : actions) {
            if (isActionPossible(world, action, player)) {
                String cmd = formatExtendedCommand(world, action, player);
                if (cmd != null && !seenCommands.contains(cmd)) {
                    seenCommands.add(cmd);
                    possibleCommands.add(cmd);
                }
            }
        }
        return possibleCommands;
    }

    private boolean isActionPossible(GameWorld world, edu.uob.GameActionNode action, Players player) {
        if (action.getSubjects() == null || action.getKeyphrases() == null || action.getKeyphrases().isEmpty()) {
            return false;
        }

        // All subjects must exist in context (inventory or current location / paths)
        for (String subject : action.getSubjects()) {
            if (!edu.uob.ExecuteExtendedCommands.consumableExistsInContext(world, subject, player)) {
                return false;
            }
        }

        // All consumed entities must exist in context (or health > 0)
        if (action.getConsumed() != null) {
            for (String consumed : action.getConsumed()) {
                if (consumed.equalsIgnoreCase("health")) {
                    if (player.getPlayerHealth() <= 0) {
                        return false;
                    }
                } else if (!edu.uob.ExecuteExtendedCommands.consumableExistsInContext(world, consumed, player)) {
                    return false;
                }
            }
        }

        return true;
    }

    private String formatExtendedCommand(GameWorld world, edu.uob.GameActionNode action, Players player) {
        String trigger = action.getKeyphrases().get(0);
        List<String> subjects = action.getSubjects();
        if (subjects == null || subjects.isEmpty()) {
            return trigger;
        }
        if (subjects.size() == 1) {
            return trigger + " " + subjects.get(0);
        }
        if (subjects.size() == 2) {
            String s0 = subjects.get(0);
            String s1 = subjects.get(1);
            boolean s0InInv = player.getPlayerInventory() != null && player.getPlayerInventory().containsKey(s0.toLowerCase());
            boolean s1InInv = player.getPlayerInventory() != null && player.getPlayerInventory().containsKey(s1.toLowerCase());

            if (s1InInv && !s0InInv) {
                return trigger + " " + s0 + " with " + s1;
            } else if (s0InInv && !s1InInv) {
                return trigger + " " + s1 + " with " + s0;
            } else {
                return trigger + " " + s0 + " with " + s1;
            }
        }
        return trigger + " " + String.join(" ", subjects);
    }

    private GameSessionResponse toGameSessionResponse(GameWorld world) {
        String startingLoc = (world.getStartingLocation() != null)
                ? world.getStartingLocation().getName()
                : "unknown";
        List<String> playerNames = new ArrayList<>(world.getAllPlayers().keySet());
        return new GameSessionResponse(
                world.getWorldId(),
                world.getWorldName(),
                startingLoc,
                playerNames.size(),
                playerNames
        );
    }
}
