package edu.uob;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;


public class ExecuteExtendedCommands {
    public static List<GameActionNode> validExtended = new LinkedList<>();

    public static String executeExtendedCommand(String playerInput, Players currentPlayer) {
        String subjectErrorMessage = null;
        //List<GameActionNode> validExtended = new LinkedList<>();
        validExtended.clear();
        for (GameActionNode action : GameActionParser.XMLList) {
            if (action.matchesKeyphrase(playerInput)) {
                if (!inputContainsSubject(playerInput, action.getSubjects())) {
                    subjectErrorMessage = "You need to specify what you're interacting with";
                    continue;
                }
                if (!subjectAvailable(action, currentPlayer)) {
                    subjectErrorMessage = ExecuteExtendedCommands.subjectAvailabilityMessage(action, currentPlayer);
                    continue;
                }
                if (!consumedEntitiesExist(action, currentPlayer)) {
                    subjectErrorMessage = ExecuteExtendedCommands.missingConsumedEntityMessage(action, currentPlayer);
                    continue;
                }
                validExtended.add(action);
            }
        }
        if (validExtended.size() == 1) {
            GameActionNode action = validExtended.get(0);

            ExecuteExtendedCommands.consumeEntities(action, currentPlayer);
            ExecuteExtendedCommands.produceEntities(action, currentPlayer);
            for (String producedEntityName : action.getProduced()) {
                GameEntity locationEntity = GameEntityParser.allEntities.get(producedEntityName.toLowerCase());
                if (locationEntity instanceof Locations) {
                    String currentLoc = currentPlayer.currentLocation.getName();
                    ExecuteExtendedCommands.addPathToLocation(currentLoc, producedEntityName.toLowerCase());
                }
            }
            return action.narration();
        } else if (validExtended.size() > 1) {
            return "Make up your mind -  Multiple extended commands.";
        }

        if (subjectErrorMessage != null){
            return subjectErrorMessage;
        }
        return "Something went wrong. with your extended command.";
    }

    public static boolean subjectAvailable(GameActionNode action, Players currentPlayer) {
        for (String subject : action.getSubjects()) {
            if (!action.getConsumed().contains(subject) && !consumableExistsInContext(subject, currentPlayer)) {
                return false;
            }
        }
        return true;
    }

    private static String subjectAvailabilityMessage(GameActionNode action, Players currentPlayer) {
        for (String subject : action.getSubjects()) {
            if (!action.getConsumed().contains(subject) && !consumableExistsInContext(subject, currentPlayer)) {
                return String.format("Missing required subject: %s", subject);
            }
        }
        return "Subject is available";
    }

    private static String missingConsumedEntityMessage(GameActionNode action,  Players currentPlayer) {
        for (String entity : action.getConsumed()) {
            if (!consumableExistsInContext(entity, currentPlayer)) {
                return String.format("You can't do that right now - missing subject (to consume): %s", entity);
            }
        }
        return "Entity to consume is available";
    }

    public static boolean consumedEntitiesExist(GameActionNode action, Players currentPlayer) {
        for (String entity : action.getConsumed()) {
            if (!entity.equals("health") && !consumableExistsInContext(entity, currentPlayer)) {
                return false;
            }
        }
        return true;
    }

    private static boolean consumableExistsInContext(String entityName, Players currentPlayer) {
        if (currentPlayer.playerInventory.containsKey(entityName)) {
            return true;
        }
        String currentLocation = currentPlayer.currentLocation.getName();
        Map<String, Artefacts> artefactsHere = GameEntityParser.locationWithArtefacts.get(currentLocation);
        Map<String, Furnitures> furnitureHere = GameEntityParser.locationWithFurnitures.get(currentLocation);
        Map<String, Characters> charactersHere = GameEntityParser.locationWithCharacters.get(currentLocation);
        if (artefactsHere != null && artefactsHere.containsKey(entityName)) {
            return true;
        }
        if (furnitureHere != null && furnitureHere.containsKey(entityName)) {
            return true;
        }
        if (charactersHere != null && charactersHere.containsKey(entityName)) {
            return true;
        }
        if (GameEntityParser.locationPaths.get(currentLocation) != null && GameEntityParser.locationPaths.get(currentLocation).contains(entityName)) {
            return true;
        }
        return false;
    }

    private static void consumeEntities(GameActionNode action, Players currentPlayer) {
        for (String entityName : action.getConsumed()) {
            if (entityName.equals("health")) {
                currentPlayer.playerHealth--;
                if(currentPlayer.playerHealth <= 0){
                    ExecuteExtendedCommands.handlePlayerDeath(currentPlayer);
                }
            } else {
                ExecuteExtendedCommands.consumeAndStoreEntity(entityName, currentPlayer);
            }
        }
    }

    private static String handlePlayerDeath(Players currentPlayer){
        StringBuilder status = new StringBuilder();
        String currentLocation = currentPlayer.currentLocation.getName();
        Map<String, Artefacts> locationArtefacts = GameEntityParser.locationWithArtefacts.get(currentLocation);

        if (locationArtefacts == null){
            locationArtefacts = new HashMap<>();
            GameEntityParser.locationWithArtefacts.put(currentLocation, locationArtefacts);
        }
        locationArtefacts.putAll(currentPlayer.playerInventory);
        currentPlayer.playerInventory.clear();
        currentPlayer.playerHealth = 3;
        currentPlayer.currentLocation = GameEntityParser.startingLocation;

        return status.append("You have died and respawned at the cabin").toString();
    }

    public static void consumeAndStoreEntity(String entityName, Players currentPlayer) {
        Artefacts artefact = currentPlayer.playerInventory.remove(entityName);
        if (artefact != null) {
            Map<String, Artefacts> storeroom = getOrCreateStoreroomMap(GameEntityParser.locationWithArtefacts);
            storeroom.put(entityName, artefact);
            return;
        }
        Map<String, Locations> allLocations = GameEntityParser.allLocations;
        Map<String, Artefacts> artefactsHere = GameEntityParser.locationWithArtefacts.get(currentPlayer.currentLocation.getName());
        Map<String, Furnitures> furnituresHere = GameEntityParser.locationWithFurnitures.get(currentPlayer.currentLocation.getName());
        Map<String, Characters> charactersHere = GameEntityParser.locationWithCharacters.get(currentPlayer.currentLocation.getName());

        if (artefactsHere != null && artefactsHere.containsKey(entityName)) {
            artefact = artefactsHere.remove(entityName);
            if (artefact != null) {
                Map<String, Artefacts> storeroom = getOrCreateStoreroomMap(GameEntityParser.locationWithArtefacts);
                storeroom.put(entityName, artefact);
                return;
            }
        }
        if (furnituresHere != null && furnituresHere.containsKey(entityName)) {
            Furnitures furniture = furnituresHere.remove(entityName);
            if (furniture != null) {
                Map<String, Furnitures> storeroom = getOrCreateStoreroomMap(GameEntityParser.locationWithFurnitures);
                storeroom.put(entityName, furniture);
                return;
            }
        }
        if (charactersHere != null && charactersHere.containsKey(entityName)) {
            Characters character = charactersHere.remove(entityName);
            if (character != null) {
                Map<String, Characters> storeroom = getOrCreateStoreroomMap(GameEntityParser.locationWithCharacters);
                storeroom.put(entityName, character);
                return;
            }
        }
        if (allLocations.containsKey(entityName)) {
            String currentLocation = currentPlayer.currentLocation.getName();
            LinkedList<String> pathsFromCurrent = GameEntityParser.locationPaths.get(currentLocation);
            if (pathsFromCurrent != null) {
                pathsFromCurrent.remove(entityName);
            }
        }
    }

    private static void produceEntities(GameActionNode actionNode, Players currentPlayer) {
        for (String entityName : actionNode.getProduced()) {
            if (entityName.equals("health")) {
                if (currentPlayer.playerHealth < 3) {
                    currentPlayer.playerHealth++;
                }
            } else {
                ExecuteExtendedCommands.retrieveAndPlaceEntity(entityName, currentPlayer.currentLocation.getName());
            }
        }
    }

    public static void retrieveAndPlaceEntity(String entityName, String targetLocationName) {
        for (Map.Entry<String, Map<String, Artefacts>> entry : GameEntityParser.locationWithArtefacts.entrySet()){
            Map<String, Artefacts> artefacts = entry.getValue();
            if (artefacts != null && artefacts.containsKey(entityName)) {
                Artefacts artefact = artefacts.remove(entityName);

                Map<String, Artefacts> targetLocationArtefacts = GameEntityParser.locationWithArtefacts.get(targetLocationName);
                if (targetLocationArtefacts == null){
                    targetLocationArtefacts = new HashMap<>();
                    GameEntityParser.locationWithArtefacts.put(targetLocationName, targetLocationArtefacts);
                }
                targetLocationArtefacts.put(entityName, artefact);
                return;
            }
        }
        for (Map.Entry<String, Map<String, Furnitures>> entry : GameEntityParser.locationWithFurnitures.entrySet()){
            Map<String, Furnitures> furnitures = entry.getValue();
            if (furnitures != null && furnitures.containsKey(entityName)) {
                Furnitures furniture = furnitures.remove(entityName);

                Map<String, Furnitures> targetLocationFurnitures = GameEntityParser.locationWithFurnitures.get(targetLocationName);
                if (targetLocationFurnitures == null){
                    targetLocationFurnitures = new HashMap<>();
                    GameEntityParser.locationWithFurnitures.put(targetLocationName, targetLocationFurnitures);
                }
                targetLocationFurnitures.put(entityName, furniture);
                return;
            }
        }
        for (Map.Entry<String, Map<String, Characters>> entry : GameEntityParser.locationWithCharacters.entrySet()) {
            Map<String, Characters> characters = entry.getValue();
            if (characters != null && characters.containsKey(entityName)) {
                Characters character = characters.remove(entityName);

                Map<String, Characters> targetLocationChars = GameEntityParser.locationWithCharacters.get(targetLocationName);
                if (targetLocationChars == null){
                    targetLocationChars = new HashMap<>();
                    GameEntityParser.locationWithCharacters.put(targetLocationName, targetLocationChars);
                }
                targetLocationChars.put(entityName, character);
                return;
            }
        }
    }

    private static void addPathToLocation(String fromLocation, String toLocation){
        LinkedList<String> pathsFrom = GameEntityParser.locationPaths.get(fromLocation);
        if (pathsFrom == null) {
            pathsFrom = new LinkedList<>();
            GameEntityParser.locationPaths.put(fromLocation, pathsFrom);
        }
        if (!pathsFrom.contains(toLocation)) {
            pathsFrom.add(toLocation);
        }
    }

    public static boolean inputContainsSubject (String playerInput, Iterable<String> subjects){
        String lowerInput = playerInput.toLowerCase();
        for (String subject : subjects){
            String lowerSubject = subject.toLowerCase();
            int index = 0;

            while (index < lowerInput.length()){
                while (index < lowerInput.length() && !Character.isLetterOrDigit(lowerInput.charAt(index))){
                    index++;
                }
                int start = index;
                while (index < lowerInput.length() && Character.isLetterOrDigit(lowerInput.charAt(index))){
                    index++;
                }
                if (start < index){
                    String word = lowerInput.substring(start, index);
                    if (word.equals(lowerSubject)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static <T> Map<String, T> getOrCreateStoreroomMap(Map<String, Map<String, T>>locationMap){
        Map<String, T> storeroom = locationMap.get("storeroom");
        if (storeroom == null){
            storeroom = new HashMap<>();
            locationMap.put("storeroom", storeroom);
        }
        return storeroom;
    }

}
