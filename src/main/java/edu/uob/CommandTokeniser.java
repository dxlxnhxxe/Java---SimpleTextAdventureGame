package edu.uob;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandTokeniser {

    public static class TokenisedCommand {
        public LinkedList<String> tokens;
        public CommandCheckResult result;

        public TokenisedCommand(LinkedList<String> tokens, CommandCheckResult result) {
            this.tokens = tokens;
            this.result = result;
        }
    }

    public enum CommandCheckResult {
        NO_COMMAND_FOUND,
        BASIC_COMMAND,
        EXTENDED_COMMAND,
        AMBIGUOUS_COMMAND
    }

    public static TokenisedCommand tokeniseAndClassifyCommand(
            String userCommand,
            Set<String> extendedKeyphrases,
            Set<String> basicCommands,
            Map<String, String> synonyms,
            Players currentPlayer
    ) {
        GameWorld world = (currentPlayer != null && currentPlayer.getGameWorld() != null)
                ? currentPlayer.getGameWorld()
                : null;
        return tokeniseAndClassifyCommand(userCommand, extendedKeyphrases, basicCommands, synonyms, currentPlayer, world);
    }

    public static TokenisedCommand tokeniseAndClassifyCommand(
            String userCommand,
            Set<String> extendedKeyphrases,
            Set<String> basicCommands,
            Map<String, String> synonyms,
            Players currentPlayer,
            GameWorld world
    ) {
        LinkedList<String> tokens = new LinkedList<>();
        if (userCommand == null || userCommand.isBlank()) {
            return new TokenisedCommand(tokens, CommandCheckResult.NO_COMMAND_FOUND);
        }

        // Replace synonyms with their canonical equivalents using regex word boundaries
        if (synonyms != null) {
            for (Map.Entry<String, String> entry : synonyms.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                StringBuilder patternBuilder = new StringBuilder();
                patternBuilder.append("\\b");
                patternBuilder.append(Pattern.quote(key));
                patternBuilder.append("\\b");
                userCommand = userCommand.replaceAll(patternBuilder.toString(), value);
            }
        }

        // Tokenize the cleaned command string into individual words
        Scanner scanner = new Scanner(userCommand);
        while (scanner.hasNext()) {
            String token = scanner.next();
            tokens.add(token.replace("_", " "));
        }
        scanner.close();

        // Prepare for command classification
        Set<String> foundValidBasics = new HashSet<>();
        Set<String> foundValidExtended = new HashSet<>();

        String fullInput = String.join(" ", tokens);

        Map<String, Map<String, Artefacts>> locationWithArtefacts = (world != null)
                ? world.getLocationWithArtefacts()
                : GameEntityParser.locationWithArtefacts;
        Map<String, Locations> allLocations = (world != null)
                ? world.getAllLocations()
                : GameEntityParser.allLocations;
        List<GameActionNode> actionList = (world != null)
                ? world.getActions()
                : GameActionParser.XMLList;

        // Check if the command matches any valid built-in commands
        for (String token : tokens) {
            if (basicCommands.contains(token)) {
                if (token.equals("get")) {
                    if (currentPlayer != null && currentPlayer.currentLocation != null) {
                        Map<String, Artefacts> artefactsHere = locationWithArtefacts.get(currentPlayer.currentLocation.getName());
                        if (artefactsHere != null) {
                            for (String artefactTokens : tokens) {
                                for (Map.Entry<String, Artefacts> entry : artefactsHere.entrySet()) {
                                    if (artefactTokens.equals(entry.getKey())) {
                                        foundValidBasics.add(token);
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
                                    foundValidBasics.add(token);
                                }
                            }
                        }
                    }
                } else if (token.equals("goto")) {
                    if (allLocations != null) {
                        for (String locationTokens : tokens) {
                            for (Map.Entry<String, Locations> entry : allLocations.entrySet()) {
                                if (locationTokens.equals(entry.getKey())) {
                                    foundValidBasics.add(token);
                                }
                            }
                        }
                    }
                } else {
                    foundValidBasics.add(token);
                }
            }
        }

        // Check extended actions
        if (extendedKeyphrases != null) {
            for (String keyphrase : extendedKeyphrases) {
                String lowerKeyphrase = keyphrase.toLowerCase();
                Pattern keyphrasePattern = Pattern.compile(String.format("\\b%s\\b", Pattern.quote(lowerKeyphrase)));
                Matcher matcher = keyphrasePattern.matcher(fullInput);
                if (matcher.find()) {
                    for (GameActionNode action : actionList) {
                        if (action.getKeyphrases().contains(keyphrase)) {
                            if (action.matchesKeyphrase(fullInput)) {
                                foundValidExtended.add(keyphrase);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (foundValidExtended.size() == 1 && foundValidBasics.size() == 1) {
            return new TokenisedCommand(tokens, CommandCheckResult.AMBIGUOUS_COMMAND);
        }
        if (foundValidBasics.size() > 1 || foundValidExtended.size() > 1) {
            return new TokenisedCommand(tokens, CommandCheckResult.AMBIGUOUS_COMMAND);
        }
        if (foundValidBasics.size() == 1) {
            return new TokenisedCommand(tokens, CommandCheckResult.BASIC_COMMAND);
        }
        if (foundValidExtended.size() == 1) {
            return new TokenisedCommand(tokens, CommandCheckResult.EXTENDED_COMMAND);
        }
        return new TokenisedCommand(tokens, CommandCheckResult.NO_COMMAND_FOUND);
    }

    public static Set<String> buildExtendedKeyphraseSet(List<GameActionNode> actions) {
        Set<String> keyphrases = new HashSet<>();
        if (actions != null) {
            for (GameActionNode action : actions) {
                keyphrases.addAll(action.getKeyphrases());
            }
        }
        return keyphrases;
    }
}
