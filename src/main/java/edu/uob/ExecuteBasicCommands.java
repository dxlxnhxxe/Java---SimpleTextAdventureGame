package edu.uob;

import java.util.*;

public class ExecuteBasicCommands {

    public static final Set<String> basicCommands = Set.of(
            "look", "goto", "inventory", "get", "drop", "health"
    );
    public static List<String> validBuiltIn = new LinkedList<>();

    public static String executeBasicCommand (Players currentPlayer, String userCommand){
        Set<String> keyphrases = CommandTokeniser.buildExtendedKeyphraseSet(GameActionParser.XMLList);

        LinkedList<String> tokens = CommandTokeniser.tokeniseAndClassifyCommand(
                userCommand, keyphrases, basicCommands, GameServer.getAllCommandSynonyms(), currentPlayer).tokens;
        validBuiltIn.clear();
        for (String token : tokens) {
            if (basicCommands.contains(token)) {
                if (token.equals("get")) {
                    Map<String, Artefacts> artefactsHere = GameEntityParser.locationWithArtefacts.get(currentPlayer.currentLocation.getName());
                    if (artefactsHere != null) {
                        for (String artefactTokens : tokens){
                            for (Map.Entry<String, Artefacts> entry : artefactsHere.entrySet()) {
                                if (artefactTokens.equals(entry.getKey())) {
                                    validBuiltIn.add(token);
                                }
                            }
                        }
                    }

                } else if (token.equals("drop")) {
                    Map<String, Artefacts> artefactsInInventory = currentPlayer.playerInventory;
                    if (artefactsInInventory != null) {
                        for (String inventoryArtefactTokens : tokens){
                            for (Map.Entry<String, Artefacts> entry : artefactsInInventory.entrySet()) {
                                if (inventoryArtefactTokens.equals(entry.getKey())) {
                                    validBuiltIn.add(token);
                                }
                            }
                        }
                    }
                } else if (token.equals("goto")) {
                    Map<String, Locations> allLocations = GameEntityParser.allLocations;
                    if (allLocations != null) {
                        for (String locationTokens : tokens){
                            for (Map.Entry<String, Locations> entry : allLocations.entrySet()) {
                                if (locationTokens.equals(entry.getKey())) {
                                    validBuiltIn.add(token);
                                }
                            }
                        }
                    }
                }
                else {
                    //Valid basic command
                    validBuiltIn.add(token);
                }
            }
        }
        if (validBuiltIn.isEmpty()) {
            return "No recognisable basic command found.";
        }
        String command = validBuiltIn.get(0);
        switch(command){
            case "look":
                return ExecuteBasicCommands.lookAround(currentPlayer, userCommand);
            case "inventory":
                return ExecuteBasicCommands.displayInventory(currentPlayer, userCommand);
            case "get":
                return ExecuteBasicCommands.getArtefacts(userCommand, currentPlayer);
            case "drop":
                return ExecuteBasicCommands.dropArtefact(userCommand, currentPlayer);
            case "goto":
                return ExecuteBasicCommands.moveTo(userCommand, currentPlayer);
            case "health":
                return ExecuteBasicCommands.displayHealth(currentPlayer, userCommand);
            default:
                return "Unknown basic command.";
        }
    }

    public static String lookAround (Players currentPlayer, String userCommand){
        if (detectAllEntitiesInCommand(userCommand)){
            return "'look' cannot be used with specific entities.";
        }
        //Import parsed entities maps
        Map<String, Players> playersInLocation = GameEntityParser.locationWithPlayers.get(currentPlayer.currentLocation.getName());
        Map<String, Artefacts> artefacts = GameEntityParser.locationWithArtefacts.get(currentPlayer.currentLocation.getName());
        Map<String, Furnitures> furnitures = GameEntityParser.locationWithFurnitures.get(currentPlayer.currentLocation.getName());
        Map<String, Characters> characters = GameEntityParser.locationWithCharacters.get(currentPlayer.currentLocation.getName());

        StringBuilder lookList = new StringBuilder();
        lookList.append("\nYou are at: ").append("\n").append("  -").append(currentPlayer.currentLocation.toString()).append("\n");

        //Display Players in the user's location
        if (playersInLocation == null || playersInLocation.isEmpty()){
            lookList.append("\nPlayers:\n").append("  -No players found\n");
        } else {
            lookList.append("\nPlayers:\n");
            boolean foundOtherPlayers = false;
            for (Players player : playersInLocation.values()){
                if (!player.getName().equals(currentPlayer.getName())){
                    lookList.append("  - ").append(player.getName()).append("\n");
                    foundOtherPlayers = true;
                }
            }
            if (!foundOtherPlayers){
                lookList.append("  -No players found\n");
            }
        }

        //Display artefacts in the user's location
        if (artefacts == null || artefacts.isEmpty()) {
            lookList.append("\nArtefacts:\n").append("  -No artefacts found\n");
        } else {
            lookList.append("\nArtefacts:\n");
            for(Artefacts artefactsInLocation : artefacts.values()){
                lookList.append("  -").append(artefactsInLocation.toString()).append("\n");
            }
        }

        //Display furniture in the user's location
        if (furnitures == null || furnitures.isEmpty()) {
            lookList.append("\nFurnitures:\n").append("  -No furnitures found\n");
        } else {
            lookList.append("\nFurnitures:").append("\n");
            for(Furnitures furnituresInLocation : furnitures.values()){
                lookList.append("  -").append(furnituresInLocation.toString()).append("\n");
            }
        }

        //Display characters in the user's location
        if (characters == null || characters.isEmpty()) {
            lookList.append("\nCharacters:\n").append("  -No Characters found\n");
        } else {
            lookList.append("\nCharacters:\n");
            for(Characters charactersInLocation : characters.values()){
                lookList.append("  -").append(charactersInLocation.toString()).append("\n");
            }
        }

        //Display paths in the user's location
        LinkedList<String> Paths = GameEntityParser.locationPaths.get(currentPlayer.currentLocation.getName());
        lookList.append("\nPaths:\n");
        if (Paths == null || Paths.isEmpty()){
            lookList.append("  -No paths found\n");
        } else {
            String pathList = String.join(", ", Paths);
            lookList.append("  -").append(pathList).append("\n");
        }

        //Storeroom entities
        lookList.append("\nStoreroom:\n");

        // Display artefacts in storeroom
        Map<String, Artefacts> storeroomArtefacts = GameEntityParser.locationWithArtefacts.get("storeroom");
        if (storeroomArtefacts == null || storeroomArtefacts.isEmpty()) {
            lookList.append("- No artefacts in storeroom\n");
        } else {
            lookList.append("Artefacts:\n");
            for (Artefacts artefact : storeroomArtefacts.values()) {
                lookList.append("- ").append(artefact.toString()).append("\n");
            }
        }


        // Display furniture in storeroom
        Map<String, Furnitures> storeroomFurnitures = GameEntityParser.locationWithFurnitures.get("storeroom");
        if (storeroomFurnitures == null || storeroomFurnitures.isEmpty()) {
            lookList.append("Furnitures:\n- No furnitures in storeroom\n");
        } else {
            lookList.append("Furnitures:\n");
            for (Furnitures furniture : storeroomFurnitures.values()) {
                lookList.append("- ").append(furniture.toString()).append("\n");
            }
        }

        // Display characters
        Map<String, Characters> storeroomCharacters = GameEntityParser.locationWithCharacters.get("storeroom");
        if (storeroomCharacters == null || storeroomCharacters.isEmpty()) {
            lookList.append("Characters:\n- No characters in storeroom\n");
        } else {
            lookList.append("Characters:\n");
            for (Characters character : storeroomCharacters.values()) {
                lookList.append("- ").append(character.toString()).append("\n");
            }
        }
        return lookList.toString();
    }

    public static String displayInventory (Players currentPlayer, String userCommand){
        if (detectAllEntitiesInCommand(userCommand)){
            return "'inventory' cannot be used with specific entities";
        }
        if(currentPlayer.playerInventory.isEmpty()){
            return "\nInventory is empty\n";
        }
        StringBuilder inventoryList = new StringBuilder("\nInventory:\n");
        for(Artefacts artefact : currentPlayer.playerInventory.values()){
            inventoryList.append(artefact.toString()).append("\n");
        }
        return inventoryList.toString();
    }

    public static String displayHealth (Players currentPlayer, String userCommand){
        if (detectAllEntitiesInCommand(userCommand)){
            return "'health' cannot be used with specific entities.";
        }
        return currentPlayer.playerHealth.toString();
    }

    public static String moveTo (String userCommand, Players currentPlayer){
        LinkedList<String> matchedDestination = ExecuteBasicCommands.extractDestination(userCommand);

        if (matchedDestination.isEmpty()) {
            return "No valid destination found.";
        }
        if (matchedDestination.size() > 1) {
            return "Ambiguous destination.";
        }

        String destinationToMoveTo = matchedDestination.getFirst();
        String currentLocationName = currentPlayer.currentLocation.getName();
        LinkedList<String> possibleDestinations = GameEntityParser.locationPaths.get(currentLocationName);
        Map<String, Players> oldLocationPlayers = GameEntityParser.locationWithPlayers.get(currentPlayer.currentLocation.getName());
        Map<String, Players> newLocationPlayers = GameEntityParser.locationWithPlayers.get(destinationToMoveTo);
        Locations newLocation = GameEntityParser.allLocations.get(destinationToMoveTo);
        if (possibleDestinations == null || !possibleDestinations.contains(destinationToMoveTo)) {
            return "You can't go there from here";
        }
        if (newLocation == null){
            return "That location does not exist";
        }
        if (oldLocationPlayers != null) {
            oldLocationPlayers.remove(currentPlayer.getName());
        }
        currentPlayer.currentLocation = newLocation;

        if (newLocationPlayers == null) {
            newLocationPlayers = new HashMap<>();
            GameEntityParser.locationWithPlayers.put(destinationToMoveTo, newLocationPlayers);
        }
        newLocationPlayers.put(currentPlayer.getName(), currentPlayer);
        return "You moved to the desired location";
    }

    public static LinkedList<String> extractDestination (String userCommand){
        LinkedList<String> wordTokens = ExecuteBasicCommands.tokeniseCommand(userCommand);
        Set<String> destinations = new HashSet<>();
        for (LinkedList<String> paths : GameEntityParser.locationPaths.values()) {
            destinations.addAll(paths);
        }
        LinkedList<String>matchedDestinations = new LinkedList<>();
        for (String token : wordTokens){
            if (destinations.contains(token)){
                matchedDestinations.add(token);
            }
        }
        return matchedDestinations;
    }

    public static String getArtefacts (String userCommand, Players currentPlayer){
        LinkedList<String> wordTokens = ExecuteBasicCommands.tokeniseCommand(userCommand);
        LinkedList<String> artefactsFiltered = ExecuteBasicCommands.filterArtefacts(wordTokens);

        if (artefactsFiltered.size() > 1){
            return "You can only get one item at a time.";
        } else if(artefactsFiltered.isEmpty()){
            return "Did not find that artefact to get.";
        }
        String artefactToGet = artefactsFiltered.getFirst();
        Map<String, Artefacts> artefactsHere = GameEntityParser.locationWithArtefacts.get(currentPlayer.currentLocation.getName());
        if (artefactsHere == null || artefactsHere.isEmpty() || !artefactsHere.containsKey(artefactToGet)){
            return String.format("There is no %s here to take", artefactToGet);
        }
        Artefacts artefact = artefactsHere.remove(artefactToGet);
        currentPlayer.playerInventory.put(artefactToGet, artefact);

        return String.format("You picked up the %s ", artefactToGet);
    }


    public static String dropArtefact (String userCommand, Players currentPlayer){
        LinkedList<String> wordTokens = ExecuteBasicCommands.tokeniseCommand(userCommand);
        LinkedList<String> artefactsFiltered = ExecuteBasicCommands.filterArtefacts(wordTokens);

        if (artefactsFiltered.size() > 1){
            return "You can only drop one item at a time.";
        } else if (artefactsFiltered.isEmpty()){
            return "Did not find that artefact to drop.";
        }

        String artefactToDrop = artefactsFiltered.getFirst();
        Artefacts artefactInInventory = currentPlayer.playerInventory.remove(artefactToDrop);

        if (artefactInInventory == null){
            return String.format("You don't have the %s", artefactToDrop);
        }

        Map<String, Artefacts> artefactsMap = GameEntityParser.locationWithArtefacts.get(currentPlayer.currentLocation.getName());
        currentPlayer.playerInventory.remove(artefactToDrop);
        if (artefactsMap == null){
            artefactsMap = new HashMap<>();
            GameEntityParser.locationWithArtefacts.put(currentPlayer.currentLocation.getName(), artefactsMap);
        }
        artefactsMap.put(artefactToDrop, artefactInInventory);
        return String.format("You dropped the %s", artefactToDrop);
    }

    public static LinkedList<String> filterArtefacts (LinkedList<String> allWords){
        Set<String> artefactNames = GameEntityParser.artefactName;
        LinkedList<String> artefactInCommand = new LinkedList<>();

        for(String word : allWords){
            if(artefactNames.contains(word)){
                artefactInCommand.add(word);
            }
        }
        return artefactInCommand;
    }

    private static boolean detectAllEntitiesInCommand (String userCommand){
        LinkedList<String> tokens = ExecuteBasicCommands.tokeniseCommand(userCommand);

        Set<String> allEntities = new HashSet<>();
        if (GameEntityParser.allEntities != null) {
            allEntities.addAll(GameEntityParser.allEntities.keySet());
        }

        for (String token : tokens) {
            if (allEntities.contains(token)) {
                return true;
            }
        }
        return false;
    }

    public static LinkedList<String> tokeniseCommand (String userCommand) {
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
