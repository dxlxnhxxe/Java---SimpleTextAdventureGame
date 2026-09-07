package edu.uob;

import java.util.*;

public class ExecuteBasicCommands {

    public static final Set<String> basicCommands = Set.of(
            "look", "goto", "inventory", "get", "drop", "health"
    );
    public static List<String> validBuiltIn = new LinkedList<>();

    public static String executeBasicCommand(Players currentPlayer, String userCommand) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return executeBasicCommand(world, currentPlayer, userCommand);
    }

    public static String executeBasicCommand(GameWorld world, Players currentPlayer, String userCommand) {
        List<GameActionNode> actions = (world != null) ? world.getActions() : GameActionParser.XMLList;
        Map<String, String> synonyms = (world != null) ? world.getAllCommandSynonyms() : GameServer.getAllCommandSynonyms();
        Set<String> keyphrases = CommandTokeniser.buildExtendedKeyphraseSet(actions);

        LinkedList<String> tokens = CommandTokeniser.tokeniseAndClassifyCommand(
                userCommand, keyphrases, basicCommands, synonyms, currentPlayer, world).tokens;

        List<String> matchedCommands = new LinkedList<>();

        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Map<String, Locations> allLocations = (world != null)
                ? world.getAllLocations()
                : GameEntityParser.allLocations;

        for (String token : tokens) {
            if (basicCommands.contains(token)) {
                if (token.equals("get")) {
                    if (currentPlayer != null && currentPlayer.currentLocation != null) {
                        Map<String, Artefacts> artefactsHere = locationWithArtefacts.get(currentPlayer.currentLocation.getName());
                        if (artefactsHere != null) {
                            for (String artefactTokens : tokens) {
                                for (Map.Entry<String, Artefacts> entry : artefactsHere.entrySet()) {
                                    if (artefactTokens.equals(entry.getKey())) {
                                        matchedCommands.add(token);
                                    }
                                }
                            }
                        }
                    }
                } else if (token.equals("drop")) {
                    if (currentPlayer != null && currentPlayer.playerInventory != null) {
                        Map<String, Artefacts> artefactsInInventory = currentPlayer.playerInventory;
                        for (String inventoryArtefactTokens : tokens) {
                            for (Map.Entry<String, Artefacts> entry : artefactsInInventory.entrySet()) {
                                if (inventoryArtefactTokens.equals(entry.getKey())) {
                                    matchedCommands.add(token);
                                }
                            }
                        }
                    }
                } else if (token.equals("goto")) {
                    if (allLocations != null) {
                        for (String locationTokens : tokens) {
                            for (Map.Entry<String, Locations> entry : allLocations.entrySet()) {
                                if (locationTokens.equals(entry.getKey())) {
                                    matchedCommands.add(token);
                                }
                            }
                        }
                    }
                } else {
                    matchedCommands.add(token);
                }
            }
        }
        if (matchedCommands.isEmpty()) {
            return "No recognisable basic command found.";
        }
        String command = matchedCommands.get(0);
        switch (command) {
            case "look":
                return lookAround(world, currentPlayer, userCommand);
            case "inventory":
                return displayInventory(world, currentPlayer, userCommand);
            case "get":
                return getArtefacts(world, userCommand, currentPlayer);
            case "drop":
                return dropArtefact(world, userCommand, currentPlayer);
            case "goto":
                return moveTo(world, userCommand, currentPlayer);
            case "health":
                return displayHealth(world, currentPlayer, userCommand);
            default:
                return "Unknown basic command.";
        }
    }

    public static String lookAround(Players currentPlayer, String userCommand) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return lookAround(world, currentPlayer, userCommand);
    }

    public static String lookAround(GameWorld world, Players currentPlayer, String userCommand) {
        if (detectAllEntitiesInCommand(world, userCommand)) {
            return "'look' cannot be used with specific entities.";
        }

        Map<String, Map<String, Players>> locationWithPlayers = (world != null)
                ? world.getLocationWithPlayers()
                : GameEntityParser.locationWithPlayers;
        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Map<String, Map<String, Furnitures>> locationWithFurnitures = (world != null)
                ? world.getLocationWithFurnitures()
                : GameEntityParser.locationWithFurnitures;
        Map<String, Map<String, Characters>> locationWithCharacters = (world != null)
                ? world.getLocationWithCharacters()
                : GameEntityParser.locationWithCharacters;
        Map<String, LinkedList<String>> locationPaths = (world != null)
                ? world.getLocationPaths()
                : GameEntityParser.locationPaths;

        String locName = currentPlayer.currentLocation.getName();
        Map<String, Players> playersInLocation = locationWithPlayers.get(locName);
        Map<String, Artefacts> artefacts = locationWithArtefacts.get(locName);
        Map<String, Furnitures> furnitures = locationWithFurnitures.get(locName);
        Map<String, Characters> characters = locationWithCharacters.get(locName);

        StringBuilder lookList = new StringBuilder();
        lookList.append("\nYou are at: \n  -").append(currentPlayer.currentLocation.toString()).append("\n");

        // Display Players in the user's location
        if (playersInLocation == null || playersInLocation.isEmpty()) {
            lookList.append("\nPlayers:\n  -No players found\n");
        } else {
            lookList.append("\nPlayers:\n");
            boolean foundOtherPlayers = false;
            for (Players player : playersInLocation.values()) {
                if (!player.getName().equals(currentPlayer.getName())) {
                    lookList.append("  - ").append(player.getName()).append("\n");
                    foundOtherPlayers = true;
                }
            }
            if (!foundOtherPlayers) {
                lookList.append("  -No players found\n");
            }
        }

        // Display artefacts in the user's location
        if (artefacts == null || artefacts.isEmpty()) {
            lookList.append("\nArtefacts:\n  -No artefacts found\n");
        } else {
            lookList.append("\nArtefacts:\n");
            for (Artefacts artefactsInLocation : artefacts.values()) {
                lookList.append("  -").append(artefactsInLocation.toString()).append("\n");
            }
        }

        // Display furniture in the user's location
        if (furnitures == null || furnitures.isEmpty()) {
            lookList.append("\nFurnitures:\n  -No furnitures found\n");
        } else {
            lookList.append("\nFurnitures:\n");
            for (Furnitures furnituresInLocation : furnitures.values()) {
                lookList.append("  -").append(furnituresInLocation.toString()).append("\n");
            }
        }

        // Display characters in the user's location
        if (characters == null || characters.isEmpty()) {
            lookList.append("\nCharacters:\n  -No Characters found\n");
        } else {
            lookList.append("\nCharacters:\n");
            for (Characters charactersInLocation : characters.values()) {
                lookList.append("  -").append(charactersInLocation.toString()).append("\n");
            }
        }

        // Display paths in the user's location
        LinkedList<String> Paths = locationPaths.get(locName);
        lookList.append("\nPaths:\n");
        if (Paths == null || Paths.isEmpty()) {
            lookList.append("  -No paths found\n");
        } else {
            String pathList = String.join(", ", Paths);
            lookList.append("  -").append(pathList).append("\n");
        }

//        // Storeroom entities
//        lookList.append("\nStoreroom:\n");
//
//        Map<String, Artefacts> storeroomArtefacts = locationWithArtefacts.get("storeroom");
//        if (storeroomArtefacts == null || storeroomArtefacts.isEmpty()) {
//            lookList.append("- No artefacts in storeroom\n");
//        } else {
//            lookList.append("Artefacts:\n");
//            for (Artefacts artefact : storeroomArtefacts.values()) {
//                lookList.append("- ").append(artefact.toString()).append("\n");
//            }
//        }
//
//        Map<String, Furnitures> storeroomFurnitures = locationWithFurnitures.get("storeroom");
//        if (storeroomFurnitures == null || storeroomFurnitures.isEmpty()) {
//            lookList.append("Furnitures:\n- No furnitures in storeroom\n");
//        } else {
//            lookList.append("Furnitures:\n");
//            for (Furnitures furniture : storeroomFurnitures.values()) {
//                lookList.append("- ").append(furniture.toString()).append("\n");
//            }
//        }
//
//        Map<String, Characters> storeroomCharacters = locationWithCharacters.get("storeroom");
//        if (storeroomCharacters == null || storeroomCharacters.isEmpty()) {
//            lookList.append("Characters:\n- No characters in storeroom\n");
//        } else {
//            lookList.append("Characters:\n");
//            for (Characters character : storeroomCharacters.values()) {
//                lookList.append("- ").append(character.toString()).append("\n");
//            }
//        }

        return lookList.toString();
    }

    public static String displayInventory(Players currentPlayer, String userCommand) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return displayInventory(world, currentPlayer, userCommand);
    }

    public static String displayInventory(GameWorld world, Players currentPlayer, String userCommand) {
        if (detectAllEntitiesInCommand(world, userCommand)) {
            return "'inv' cannot be used with specific entities.";
        }
        if (currentPlayer.playerInventory == null || currentPlayer.playerInventory.isEmpty()) {
            return "Your inventory is currently empty.";
        }
        StringBuilder inventoryList = new StringBuilder();
        inventoryList.append("Inventory:\n");
        for (Artefacts artefact : currentPlayer.playerInventory.values()) {
            inventoryList.append("  -").append(artefact.toString()).append("\n");
        }
        return inventoryList.toString();
    }

    public static String displayHealth(Players currentPlayer, String userCommand) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return displayHealth(world, currentPlayer, userCommand);
    }

    public static String displayHealth(GameWorld world, Players currentPlayer, String userCommand) {
        if (detectAllEntitiesInCommand(world, userCommand)) {
            return "'health' cannot be used with specific entities.";
        }
        if (currentPlayer.playerHealth == null) {
            return "Player health is currently unavailable";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Health: ").append(currentPlayer.playerHealth);
        if (currentPlayer.playerHealth <= 1) {
            sb.append(" (reduced)");
        } else if (currentPlayer.playerHealth >= 3) {
            sb.append(" (higher)");
        }
        return sb.toString();
    }

    public static String moveTo(String userCommand, Players currentPlayer) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return moveTo(world, userCommand, currentPlayer);
    }

    public static String moveTo(GameWorld world, String userCommand, Players currentPlayer) {
        Map<String, LinkedList<String>> locationPaths = (world != null)
                ? world.getLocationPaths()
                : GameEntityParser.locationPaths;
        Map<String, Locations> allLocations = (world != null)
                ? world.getAllLocations()
                : GameEntityParser.allLocations;
        Map<String, Map<String, Players>> locationWithPlayers = (world != null)
                ? world.getLocationWithPlayers()
                : GameEntityParser.locationWithPlayers;

        LinkedList<String> wordTokens = tokeniseCommand(userCommand);
        LinkedList<String> availablePaths = locationPaths.get(currentPlayer.currentLocation.getName());

        if (availablePaths == null || availablePaths.isEmpty()) {
            return "No available paths from this location.";
        }

        LinkedList<String> requestedLocations = new LinkedList<>();
        for (String word : wordTokens) {
            if (allLocations.containsKey(word)) {
                requestedLocations.add(word);
            }
        }

        if (requestedLocations.isEmpty()) {
            return "You didn't specify where to go.";
        }

        if (requestedLocations.size() > 1) {
            return "You can only go to one location at a time.";
        }

        String targetLocation = requestedLocations.getFirst();

        if (availablePaths.contains(targetLocation)) {
            Locations newLocation = allLocations.get(targetLocation);
            if (newLocation != null) {
                String oldLocName = currentPlayer.currentLocation.getName();
                Map<String, Players> oldLocPlayers = locationWithPlayers.get(oldLocName);
                if (oldLocPlayers != null) {
                    oldLocPlayers.remove(currentPlayer.getName());
                }

                currentPlayer.currentLocation = newLocation;
                Map<String, Players> newLocPlayers = locationWithPlayers.computeIfAbsent(
                        targetLocation, k -> new HashMap<>());
                newLocPlayers.put(currentPlayer.getName(), currentPlayer);

                return String.format("You've made your way to the desired location: the %s...", targetLocation);
            }
        }
        return String.format("You can't get to the %s from here.", targetLocation);
    }

    public static String getArtefacts(String userCommand, Players currentPlayer) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return getArtefacts(world, userCommand, currentPlayer);
    }

    public static String getArtefacts(GameWorld world, String userCommand, Players currentPlayer) {
        LinkedList<String> wordTokens = tokeniseCommand(userCommand);
        LinkedList<String> artefactsFiltered = filterArtefacts(world, wordTokens);

        if (artefactsFiltered.size() > 1) {
            return "You can only pick up one item at a time.";
        } else if (artefactsFiltered.isEmpty()) {
            return "Did not find that artefact in current location.";
        }

        String artefactToGet = artefactsFiltered.getFirst();
        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;

        Map<String, Artefacts> artefactsMap = locationWithArtefacts.get(currentPlayer.currentLocation.getName());

        if (artefactsMap != null && artefactsMap.containsKey(artefactToGet)) {
            Artefacts artefact = artefactsMap.remove(artefactToGet);
            if (artefact != null) {
                currentPlayer.playerInventory.put(artefactToGet, artefact);
                return String.format("You picked up a %s", artefactToGet);
            }
        }
        return "Artefact is not in current location";
    }

    public static String dropArtefact(String userCommand, Players currentPlayer) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return dropArtefact(world, userCommand, currentPlayer);
    }

    public static String dropArtefact(GameWorld world, String userCommand, Players currentPlayer) {
        LinkedList<String> wordTokens = tokeniseCommand(userCommand);
        LinkedList<String> artefactsFiltered = filterArtefacts(world, wordTokens);

        if (artefactsFiltered.size() > 1) {
            return "You can only drop one item at a time.";
        } else if (artefactsFiltered.isEmpty()) {
            return "Did not find that artefact to drop.";
        }

        String artefactToDrop = artefactsFiltered.getFirst();
        Artefacts artefactInInventory = currentPlayer.playerInventory.remove(artefactToDrop);

        if (artefactInInventory == null) {
            return String.format("You don't have the %s", artefactToDrop);
        }

        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;

        Map<String, Artefacts> artefactsMap = locationWithArtefacts.computeIfAbsent(
                currentPlayer.currentLocation.getName(), k -> new HashMap<>());
        artefactsMap.put(artefactToDrop, artefactInInventory);
        return String.format("You dropped the %s", artefactToDrop);
    }

    public static LinkedList<String> filterArtefacts(LinkedList<String> allWords) {
        return filterArtefacts(null, allWords);
    }

    public static LinkedList<String> filterArtefacts(GameWorld world, LinkedList<String> allWords) {
        Set<String> artefactNames = (world != null)
                ? world.getArtefactNames()
                : GameEntityParser.artefactName;
        LinkedList<String> artefactInCommand = new LinkedList<>();

        for (String word : allWords) {
            if (artefactNames.contains(word)) {
                artefactInCommand.add(word);
            }
        }
        return artefactInCommand;
    }

    private static boolean detectAllEntitiesInCommand(GameWorld world, String userCommand) {
        LinkedList<String> tokens = tokeniseCommand(userCommand);
        Map<String, GameEntity> allEntities = (world != null)
                ? world.getAllEntities()
                : GameEntityParser.allEntities;

        if (allEntities != null) {
            for (String token : tokens) {
                if (allEntities.containsKey(token)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static LinkedList<String> tokeniseCommand(String userCommand) {
        LinkedList<String> parts = new LinkedList<>();
        int start = 0;
        for (int i = 0; i < userCommand.length(); i++) {
            if (Character.isWhitespace(userCommand.charAt(i))) {
                if (i > start) {
                    parts.add(userCommand.substring(start, i));
                }
                start = i + 1;
            }
        }
        if (start < userCommand.length()) {
            parts.add(userCommand.substring(start));
        }
        return parts;
    }
}
