package edu.uob;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CommandTokeniser{

    public static class TokenisedCommand{
        public LinkedList<String> tokens;
        public CommandCheckResult result;
        public TokenisedCommand(LinkedList<String> tokens, CommandCheckResult result) {
            this.tokens = tokens;
            this.result = result;
        }
    }

    public enum CommandCheckResult{
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
    )
    {
        LinkedList<String> tokens = new LinkedList<>();
        //Return early if the command is null or blank
        if (userCommand == null || userCommand.isBlank()) {
            return new TokenisedCommand(tokens, CommandCheckResult.NO_COMMAND_FOUND);
        }

        //Replace synonyms with their canonical equivalents using regex word boundaries
        for (Map.Entry<String, String> entry : synonyms.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            StringBuilder patternBuilder = new StringBuilder();
            patternBuilder.append("\\b");
            patternBuilder.append(Pattern.quote(key));
            patternBuilder.append("\\b");
            userCommand = userCommand.replaceAll(patternBuilder.toString(), value);
        }

        //Tokenize the cleaned command string into individual words
        Scanner scanner = new Scanner(userCommand);
        while(scanner.hasNext()) {
            String token = scanner.next();
            tokens.add(token.replace("_", " "));
        }
        scanner.close();

        //Prepare for command classification
        Set<String> foundValidBasics = new HashSet<>();
        Set<String> foundValidExtended = new HashSet<>();

        //Rebuild the full command string
        String fullInput = String.join(" ", tokens);

        //Check if the command matches any valid built in commands
        for (String token : tokens){
            if (basicCommands.contains(token)) {
                if (token.equals("get")) {
                    Map<String, Artefacts> artefactsHere = GameEntityParser.locationWithArtefacts
                            .get(currentPlayer.currentLocation.getName());
                    if (artefactsHere != null) {
                        for (String artefactTokens : tokens){
                            for (Map.Entry<String, Artefacts> entry : artefactsHere.entrySet()) {
                                if (artefactTokens.equals(entry.getKey())) {
                                    foundValidBasics.add(token);
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
                                    foundValidBasics.add(token);
                                }
                            }
                        }
                    }
                }
                else if (token.equals("goto")) {
                    Map<String, Locations> allLocations = GameEntityParser.allLocations;
                    if (allLocations != null) {
                        for (String locationTokens : tokens){
                            for (Map.Entry<String, Locations> entry : allLocations.entrySet()) {
                                if (locationTokens.equals(entry.getKey())) {
                                    foundValidBasics.add(token);
                                }
                            }
                        }
                    }
                }
                else {
                    //Valid basic command
                    foundValidBasics.add(token);
                }
            }
        }

        //For each extended keyphrase, check if it exists as a whole phrase in the user input
        for (String keyphrase : extendedKeyphrases) {
            String lowerKeyphrase = keyphrase.toLowerCase();
            //Compile a pattern to match the keyphrase as a whole phrase match
            Pattern keyphrasePattern = Pattern.compile(String.format("\\b%s\\b", Pattern.quote(lowerKeyphrase)));
            Matcher matcher = keyphrasePattern.matcher(fullInput);
            if (matcher.find()) {
                //If the keyphrase was found, look for matching extended actions in the parsed XML list
                for (GameActionNode action : GameActionParser.XMLList) {
                    if (action.getKeyphrases().contains(keyphrase)) {
                        //Extract tokens present in the input that match known game entities
                        Set<String> inputEntities = new HashSet<>();
                        for (String token : tokens) {
                            if (GameEntityParser.allEntities.containsKey(token)) {
                                inputEntities.add(token);
                            }
                        }
                        //Convert expected subjects from the action to lowercase
                        Set<String> expectedSubjects = new HashSet<>();
                        for (String subject : action.getSubjects()) {
                            expectedSubjects.add(subject.toLowerCase());
                        }
                        //Skip this action if the input contains entities not required by the action
                        if (!expectedSubjects.containsAll(inputEntities)) {
                            continue;
                        }
                        //Check if there's at least one matching subject between the input and the actions expected subjects
                        boolean partialSubjectMatch = !Collections.disjoint(inputEntities, expectedSubjects);

                        //Confirm the action matches the full keyphrase, has at least one subject,
                        // and the player has the required entities to perform the action
                        if (partialSubjectMatch && action.matchesKeyphrase(fullInput) &&
                                ExecuteExtendedCommands.consumedEntitiesExist(action, currentPlayer)) {
                            foundValidExtended.add(keyphrase);
                            //Stop searching after a valid match has been found
                            break;
                        }
                    }
                }
            }
        }

        //Return the result based on the match
        if (foundValidExtended.size() == 1 && foundValidBasics.size() == 1) {
            return new TokenisedCommand(tokens, CommandCheckResult.AMBIGUOUS_COMMAND);
        }
        if (foundValidBasics.size() > 1) {
            return new  TokenisedCommand(tokens, CommandCheckResult.AMBIGUOUS_COMMAND);
        }
        if (foundValidExtended.size() > 1) {
            return new  TokenisedCommand(tokens, CommandCheckResult.AMBIGUOUS_COMMAND);
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
        for (GameActionNode action: actions) {
            keyphrases.addAll(action.getKeyphrases());
        }
        return keyphrases;
    }
}
