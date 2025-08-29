package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;
import edu.uob.CommandTokeniser.TokenisedCommand;
import edu.uob.CommandTokeniser.CommandCheckResult;

public final class GameServer {
    private static final char END_OF_TRANSMISSION = 4;
    public static void main(String[] args) throws IOException {
        StringBuilder entitiesPath = new StringBuilder();
        entitiesPath.append("config");
        entitiesPath.append(File.separator);
        entitiesPath.append("extended-entities.dot");
        File entitiesFile = Paths.get(entitiesPath.toString()).toAbsolutePath().toFile();

        StringBuilder actionsPath = new StringBuilder();
        actionsPath.append("config");
        actionsPath.append(File.separator);
        actionsPath.append("extended-actions.xml");
        File actionsFile = Paths.get(actionsPath.toString()).toAbsolutePath().toFile();

        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    public static final Map<String, String> allCommandSynonyms = new HashMap<>();
    public final Map<String, Players> allPlayersInServer = new HashMap<>();
    public static Map<String, String> getAllCommandSynonyms() {
        return allCommandSynonyms;
    }
    public GameServer(File entitiesFile, File actionsFile) {
        GameActionParser.extendedCommands.clear();
        GameEntityParser.parseEntities(entitiesFile);
        GameActionParser.parseXML(actionsFile);

        allCommandSynonyms.put("inv", "inventory");
        allCommandSynonyms.put("inventory", "inventory");
        allCommandSynonyms.putAll(GameActionParser.extendedKeyphraseSynonyms);
    }

    public String handleCommand(String command) {
        int colonIndex = command.indexOf(':');
        if (colonIndex == -1) {
            return "ERROR: Invalid command format. Missing ':' between username and command.";
        }
        String username = command.substring(0, colonIndex).trim();
        if (!username.matches("[a-zA-Z\\s'-]+")) {
            return "ERROR: Invalid username format.";
        }

        Players currentPlayer = allPlayersInServer.get(username);
        if (currentPlayer == null) {
            currentPlayer = new Players(username, "A new Player");
            allPlayersInServer.put(username, currentPlayer);

            // Place player in their starting location
            String locationName = currentPlayer.currentLocation.getName();
            Map<String, Players> playersInLocation = GameEntityParser.locationWithPlayers.get(locationName);
            if (playersInLocation == null) {
                playersInLocation = new HashMap<>();
                GameEntityParser.locationWithPlayers.put(locationName, playersInLocation);
            }
            playersInLocation.put(currentPlayer.getName(), currentPlayer);
        }
        String userCommand = command.substring(Math.addExact(colonIndex, 1)).trim().replaceAll("\\s+", " ").toLowerCase();
        if (userCommand.isEmpty()) {
            return "ERROR: Empty command after username.";
        }

        //Build keyphrase set from all extended actions
        Set<String> keyphrases = CommandTokeniser.buildExtendedKeyphraseSet(GameActionParser.XMLList);
        TokenisedCommand tokenised = CommandTokeniser.tokeniseAndClassifyCommand(userCommand, keyphrases,
                ExecuteBasicCommands.basicCommands, allCommandSynonyms, currentPlayer);
        CommandCheckResult result = tokenised.result;
        switch (result) {
            case BASIC_COMMAND:
                return ExecuteBasicCommands.executeBasicCommand(currentPlayer, userCommand);
            case EXTENDED_COMMAND:
                return ExecuteExtendedCommands.executeExtendedCommand(userCommand, currentPlayer);
            case AMBIGUOUS_COMMAND:
                return "ERROR: Ambiguous command.";
            case NO_COMMAND_FOUND:
                return "ERROR: No recognizable command found.";
        }
        return "ERROR: Unhandled command scenario.";
    }

    public void blockingListenOn(int portNumber) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            StringBuilder portMessage = new StringBuilder();
            portMessage.append("Listening on port ").append(portNumber);
            System.out.println(portMessage);
            while (true) {
                final Socket clientSocket = serverSocket.accept();
                StringBuilder clientMessage = new StringBuilder();
                clientMessage.append("Accepted connection from ").append(clientSocket.getInetAddress().getHostName());
                System.out.println(clientMessage);
                Thread clientThread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            this.handleClient(clientSocket);
                        } catch (IOException e) {
                            StringBuilder IOExceptionMessage = new StringBuilder();
                            IOExceptionMessage.append("Client connection errr: ").append(e.getMessage());
                            System.out.println(IOExceptionMessage);
                        }
                    }
                    private void handleClient(Socket socket) throws IOException {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        try {
                            String command;
                            while ((command = reader.readLine()) != null){
                                StringBuilder commandMessage = new StringBuilder();
                                commandMessage.append("Received command: ").append(command);
                                System.out.println(commandMessage);
                                String result = GameServer.this.handleCommand(command);
                                writer.write(result);
                                writer.write("\n");
                                writer.write(Character.toString(GameServer.END_OF_TRANSMISSION));
                                writer.write("\n");
                                writer.flush();
                            }
                        } finally{
                            reader.close();
                            writer.close();
                            socket.close();
                        }
                    }
                });
                clientThread.start();
            }
        }
    }

    private void blockingHandleConnection(ServerSocket serverSocket) throws IOException {
        try (Socket s = serverSocket.accept();
             BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()))) {
            System.out.println("Connection established");
            String incomingCommand = reader.readLine();
            if(incomingCommand != null) {
                StringBuilder logMessage = new StringBuilder();
                logMessage.append("Received message from: ");
                logMessage.append(incomingCommand);
                System.out.println(logMessage);
                String result = this.handleCommand(incomingCommand);
                writer.write(result);
                writer.write("\n");
                writer.write(Character.toString(END_OF_TRANSMISSION));
                writer.write("\n");
                writer.flush();
            }
        }
    }
}
