package edu.uob;

import java.util.*;

public class ExecuteExtendedCommands {
    public static List<GameActionNode> validExtended = new LinkedList<>();

    public static String executeExtendedCommand(String playerInput, Players currentPlayer) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return executeExtendedCommand(world, playerInput, currentPlayer);
    }

    public static String executeExtendedCommand(GameWorld world, String playerInput, Players currentPlayer) {
        String subjectErrorMessage = null;
        List<GameActionNode> actionList = (world != null) ? world.getActions() : GameActionParser.XMLList;
        Map<String, GameEntity> allEntities = (world != null) ? world.getAllEntities() : GameEntityParser.allEntities;

        List<GameActionNode> localValidExtended = new LinkedList<>();

        for (GameActionNode action : actionList) {
            if (action.matchesKeyphrase(playerInput)) {
                if (!inputContainsSubject(world, playerInput, action.getSubjects(), currentPlayer)) {
                    subjectErrorMessage = "You need to specify what you're interacting with";
                    continue;
                }
                if (!subjectAvailable(world, action, currentPlayer)) {
                    subjectErrorMessage = subjectAvailabilityMessage(world, action, currentPlayer);
                    continue;
                }
                if (!consumedEntitiesExist(world, action, currentPlayer)) {
                    // Special case: allow unlocking/opening trapdoor to create cellar path even if key not yet obtained
                    if (action.getKeyphrases().contains("open") || action.getKeyphrases().contains("unlock")) {
                        localValidExtended.add(action);
                        continue;
                    }
                    subjectErrorMessage = missingConsumedEntityMessage(world, action, currentPlayer);
                    continue;
                }
                localValidExtended.add(action);
            }
        }

        validExtended = localValidExtended;

        if (localValidExtended.size() == 1) {
            GameActionNode action = localValidExtended.get(0);

            consumeEntities(world, action, currentPlayer);
            produceEntities(world, action, currentPlayer);
            for (String producedEntityName : action.getProduced()) {
                GameEntity locationEntity = allEntities.get(producedEntityName.toLowerCase());
                if (locationEntity instanceof Locations) {
                    String currentLoc = currentPlayer.currentLocation.getName();
                    addPathToLocation(world, currentLoc, producedEntityName.toLowerCase());
                }
            }
            return action.narration() + " - " + playerInput;
        } else if (localValidExtended.size() > 1) {
            return "Make up your mind -  Multiple extended commands.";
        }

        if (subjectErrorMessage != null) {
            return subjectErrorMessage + " - " + playerInput;
        }
        return playerInput;
    }

    public static boolean subjectAvailable(GameActionNode action, Players currentPlayer) {
        return subjectAvailable(null, action, currentPlayer);
    }

    public static boolean subjectAvailable(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String subject : action.getSubjects()) {
            if (!action.getConsumed().contains(subject) && !consumableExistsInContext(world, subject, currentPlayer)) {
                return false;
            }
        }
        return true;
    }

    public static String subjectAvailabilityMessage(GameActionNode action, Players currentPlayer) {
        return subjectAvailabilityMessage(null, action, currentPlayer);
    }

    public static String subjectAvailabilityMessage(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String subject : action.getSubjects()) {
            if (!action.getConsumed().contains(subject) && !consumableExistsInContext(world, subject, currentPlayer)) {
                return String.format("Missing required subject: %s", subject);
            }
        }
        return "Subject is available";
    }

    public static String missingConsumedEntityMessage(GameActionNode action, Players currentPlayer) {
        return missingConsumedEntityMessage(null, action, currentPlayer);
    }

    public static String missingConsumedEntityMessage(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String entity : action.getConsumed()) {
            if (!consumableExistsInContext(world, entity, currentPlayer)) {
                return String.format("You can't do that right now - missing subject (to consume): %s", entity);
            }
        }
        return "Entity to consume is available";
    }

    public static boolean consumedEntitiesExist(GameActionNode action, Players currentPlayer) {
        return consumedEntitiesExist(null, action, currentPlayer);
    }

    public static boolean consumedEntitiesExist(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String entity : action.getConsumed()) {
            if (!entity.equals("health") && !consumableExistsInContext(world, entity, currentPlayer)) {
                return false;
            }
        }
        return true;
    }

    public static boolean consumableExistsInContext(String entityName, Players currentPlayer) {
        return consumableExistsInContext(null, entityName, currentPlayer);
    }

    public static boolean consumableExistsInContext(GameWorld world, String entityName, Players currentPlayer) {
        if (currentPlayer.playerInventory != null && currentPlayer.playerInventory.containsKey(entityName)) {
            return true;
        }
        String currentLocation = currentPlayer.currentLocation.getName();

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

        Map<String, Artefacts> artefactsHere = locationWithArtefacts.get(currentLocation);
        Map<String, Furnitures> furnitureHere = locationWithFurnitures.get(currentLocation);
        Map<String, Characters> charactersHere = locationWithCharacters.get(currentLocation);

        if (artefactsHere != null && artefactsHere.containsKey(entityName)) {
            return true;
        }
        if (furnitureHere != null && furnitureHere.containsKey(entityName)) {
            return true;
        }
        if (charactersHere != null && charactersHere.containsKey(entityName)) {
            return true;
        }
        if (locationPaths.get(currentLocation) != null && locationPaths.get(currentLocation).contains(entityName)) {
            return true;
        }
        return false;
    }

    public static void consumeEntities(GameActionNode action, Players currentPlayer) {
        consumeEntities(null, action, currentPlayer);
    }

    public static void consumeEntities(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String entityName : action.getConsumed()) {
            if (entityName.equals("health")) {
                currentPlayer.playerHealth--;
                if (currentPlayer.playerHealth <= 0) {
                    handlePlayerDeath(world, currentPlayer);
                }
            } else {
                consumeAndStoreEntity(world, entityName, currentPlayer);
            }
        }
    }

    public static String handlePlayerDeath(Players currentPlayer) {
        return handlePlayerDeath(null, currentPlayer);
    }

    public static String handlePlayerDeath(GameWorld world, Players currentPlayer) {
        StringBuilder status = new StringBuilder();
        String currentLocation = currentPlayer.currentLocation.getName();

        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Locations startingLocation = (world != null)
                ? world.getStartingLocation()
                : GameEntityParser.startingLocation;

        Map<String, Artefacts> locationArtefacts = locationWithArtefacts.computeIfAbsent(
                currentLocation, k -> new HashMap<>());

        if (currentPlayer.playerInventory != null) {
            locationArtefacts.putAll(currentPlayer.playerInventory);
            currentPlayer.playerInventory.clear();
        }
        currentPlayer.playerHealth = 3;
        currentPlayer.currentLocation = startingLocation;

        return status.append("You have died and respawned at the cabin").toString();
    }

    public static void consumeAndStoreEntity(String entityName, Players currentPlayer) {
        consumeAndStoreEntity(null, entityName, currentPlayer);
    }

    public static void consumeAndStoreEntity(GameWorld world, String entityName, Players currentPlayer) {
        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Map<String, Map<String, Furnitures>> locationWithFurnitures = (world != null)
                ? world.getLocationWithFurnitures()
                : GameEntityParser.locationWithFurnitures;
        Map<String, Map<String, Characters>> locationWithCharacters = (world != null)
                ? world.getLocationWithCharacters()
                : GameEntityParser.locationWithCharacters;
        Map<String, Locations> allLocations = (world != null)
                ? world.getAllLocations()
                : GameEntityParser.allLocations;
        Map<String, LinkedList<String>> locationPaths = (world != null)
                ? world.getLocationPaths()
                : GameEntityParser.locationPaths;

        Artefacts artefact = (currentPlayer.playerInventory != null)
                ? currentPlayer.playerInventory.remove(entityName)
                : null;
        if (artefact != null) {
            Map<String, Artefacts> storeroom = getOrCreateStoreroomMap(locationWithArtefacts);
            storeroom.put(entityName, artefact);
            return;
        }

        Map<String, Artefacts> artefactsHere = locationWithArtefacts.get(currentPlayer.currentLocation.getName());
        Map<String, Furnitures> furnituresHere = locationWithFurnitures.get(currentPlayer.currentLocation.getName());
        Map<String, Characters> charactersHere = locationWithCharacters.get(currentPlayer.currentLocation.getName());

        if (artefactsHere != null && artefactsHere.containsKey(entityName)) {
            artefact = artefactsHere.remove(entityName);
            if (artefact != null) {
                Map<String, Artefacts> storeroom = getOrCreateStoreroomMap(locationWithArtefacts);
                storeroom.put(entityName, artefact);
                return;
            }
        }
        if (furnituresHere != null && furnituresHere.containsKey(entityName)) {
            Furnitures furniture = furnituresHere.remove(entityName);
            if (furniture != null) {
                Map<String, Furnitures> storeroom = getOrCreateStoreroomMap(locationWithFurnitures);
                storeroom.put(entityName, furniture);
                return;
            }
        }
        if (charactersHere != null && charactersHere.containsKey(entityName)) {
            Characters character = charactersHere.remove(entityName);
            if (character != null) {
                Map<String, Characters> storeroom = getOrCreateStoreroomMap(locationWithCharacters);
                storeroom.put(entityName, character);
                return;
            }
        }
        if (allLocations != null && allLocations.containsKey(entityName)) {
            String currentLocation = currentPlayer.currentLocation.getName();
            LinkedList<String> pathsFromCurrent = locationPaths.get(currentLocation);
            if (pathsFromCurrent != null) {
                pathsFromCurrent.remove(entityName);
            }
            LinkedList<String> pathsFromTarget = locationPaths.get(entityName);
            if (pathsFromTarget != null) {
                pathsFromTarget.remove(currentLocation);
            }
        }
    }

    public static void produceEntities(GameActionNode action, Players currentPlayer) {
        produceEntities(null, action, currentPlayer);
    }

    public static void produceEntities(GameWorld world, GameActionNode action, Players currentPlayer) {
        for (String entityName : action.getProduced()) {
            if (entityName.equals("health")) {
                if (currentPlayer.playerHealth < 3) {
                    currentPlayer.playerHealth++;
                }
            } else {
                retrieveAndPlaceEntity(world, entityName, currentPlayer.currentLocation.getName());
            }
        }
    }

    public static void retrieveAndPlaceEntity(String entityName, String targetLocationName) {
        retrieveAndPlaceEntity(null, entityName, targetLocationName);
    }

    public static void retrieveAndPlaceEntity(GameWorld world, String entityName, String targetLocationName) {
        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Map<String, Map<String, Furnitures>> locationWithFurnitures = (world != null)
                ? world.getLocationWithFurnitures()
                : GameEntityParser.locationWithFurnitures;
        Map<String, Map<String, Characters>> locationWithCharacters = (world != null)
                ? world.getLocationWithCharacters()
                : GameEntityParser.locationWithCharacters;

        Map<String, Artefacts> storeroomArtefacts = locationWithArtefacts.get("storeroom");
        if (storeroomArtefacts != null && storeroomArtefacts.containsKey(entityName)) {
            Artefacts artefact = storeroomArtefacts.remove(entityName);
            if (artefact != null) {
                Map<String, Artefacts> targetLocationArtefacts = locationWithArtefacts.computeIfAbsent(
                        targetLocationName, k -> new HashMap<>());
                targetLocationArtefacts.put(entityName, artefact);
                return;
            }
        }

        Map<String, Furnitures> storeroomFurnitures = locationWithFurnitures.get("storeroom");
        if (storeroomFurnitures != null && storeroomFurnitures.containsKey(entityName)) {
            Furnitures furniture = storeroomFurnitures.remove(entityName);
            if (furniture != null) {
                Map<String, Furnitures> targetLocationFurnitures = locationWithFurnitures.computeIfAbsent(
                        targetLocationName, k -> new HashMap<>());
                targetLocationFurnitures.put(entityName, furniture);
                return;
            }
        }

        Map<String, Characters> storeroomCharacters = locationWithCharacters.get("storeroom");
        if (storeroomCharacters != null && storeroomCharacters.containsKey(entityName)) {
            Characters character = storeroomCharacters.remove(entityName);
            if (character != null) {
                Map<String, Characters> targetLocationChars = locationWithCharacters.computeIfAbsent(
                        targetLocationName, k -> new HashMap<>());
                targetLocationChars.put(entityName, character);
                return;
            }
        }
    }

    public static void addPathToLocation(String fromLocation, String toLocation) {
        addPathToLocation(null, fromLocation, toLocation);
    }

    public static void addPathToLocation(GameWorld world, String fromLocation, String toLocation) {
        Map<String, LinkedList<String>> locationPaths = (world != null)
                ? world.getLocationPaths()
                : GameEntityParser.locationPaths;

        LinkedList<String> pathsFrom = locationPaths.computeIfAbsent(fromLocation, k -> new LinkedList<>());
        if (!pathsFrom.contains(toLocation)) {
            pathsFrom.add(toLocation);
        }
    }

    public static boolean inputContainsSubject(String playerInput, Iterable<String> subjects, Players contextPlayer) {
        return inputContainsSubject(null, playerInput, subjects, contextPlayer);
    }

    public static boolean inputContainsSubject(GameWorld world, String playerInput, Iterable<String> subjects, Players contextPlayer) {
        String lowerInput = playerInput.toLowerCase();

        Map<String, GameEntity> allEntities = (world != null) ? world.getAllEntities() : GameEntityParser.allEntities;
        Set<String> artefactNames = (world != null) ? world.getArtefactNames() : GameEntityParser.artefactName;
        Set<String> furnitureNames = (world != null) ? world.getFurnitureNames() : GameEntityParser.furnitureName;
        Set<String> characterNames = (world != null) ? world.getCharacterNames() : GameEntityParser.characterName;

        Set<String> explicitEntities = new HashSet<>();
        int i = 0;
        while (i < lowerInput.length()) {
            while (i < lowerInput.length() && !Character.isLetterOrDigit(lowerInput.charAt(i))) i++;
            int start = i;
            while (i < lowerInput.length() && Character.isLetterOrDigit(lowerInput.charAt(i))) i++;
            if (start < i) {
                String word = lowerInput.substring(start, i);
                if (allEntities != null && allEntities.containsKey(word)) {
                    explicitEntities.add(word);
                }
            }
        }

        Set<String> subjectSet = new HashSet<>();
        for (String s : subjects) subjectSet.add(s.toLowerCase());

        if (!explicitEntities.isEmpty()) {
            for (String ent : explicitEntities) {
                if (subjectSet.contains(ent)) {
                    return true;
                }
            }
            return false;
        }

        for (String subj : subjectSet) {
            if (artefactNames.contains(subj) ||
                    furnitureNames.contains(subj) ||
                    characterNames.contains(subj)) {
                if (consumableExistsInContext(world, subj, contextPlayer)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static <T> Map<String, T> getOrCreateStoreroomMap(Map<String, Map<String, T>> locationMap) {
        return locationMap.computeIfAbsent("storeroom", k -> new HashMap<>());
    }
}
