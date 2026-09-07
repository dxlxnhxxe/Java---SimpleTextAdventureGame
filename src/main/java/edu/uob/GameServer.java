package edu.uob;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.*;

public final class GameServer {
    private static final char END_OF_TRANSMISSION = 4;

    private static final Map<String, String> allCommandSynonyms = new HashMap<>();
    private final Map<String, Players> allPlayersInServer = new HashMap<>();
    private final GameWorld gameWorld;

    public static void main(String[] args) throws IOException {
        File entitiesFile = Paths.get("config", "extended-entities.dot")
                .toAbsolutePath()
                .toFile();

        File actionsFile = Paths.get("config", "extended-actions.xml")
                .toAbsolutePath()
                .toFile();

        GameServer server = new GameServer(entitiesFile, actionsFile);
        server.blockingListenOn(8888);
    }

    public static Map<String, String> getAllCommandSynonyms() {
        return allCommandSynonyms;
    }

    public GameServer(File entitiesFile, File actionsFile) {
        // Reset all static game state so each server/test starts from a clean world
        GameActionParser.XMLList.clear();
        GameActionParser.extendedCommands.clear();
        GameActionParser.extendedKeyphraseSynonyms.clear();
        GameEntityParser.allLocations.clear();
        GameEntityParser.locationPaths.clear();
        GameEntityParser.startingLocation = null;
        GameEntityParser.allEntities.clear();
        GameEntityParser.locationWithPlayers.clear();
        GameEntityParser.locationWithFurnitures.clear();
        GameEntityParser.locationWithArtefacts.clear();
        GameEntityParser.locationWithCharacters.clear();
        GameEntityParser.artefactName.clear();
        GameEntityParser.furnitureName.clear();
        GameEntityParser.characterName.clear();

        GameEntityParser.parseEntities(entitiesFile);
        GameActionParser.parseXML(actionsFile);

        allCommandSynonyms.clear();
        allCommandSynonyms.put("inv", "inventory");
        allCommandSynonyms.put("inventory", "inventory");
        allCommandSynonyms.putAll(GameActionParser.extendedKeyphraseSynonyms);

        this.gameWorld = GameWorld.createFromFiles(entitiesFile, actionsFile);
    }

    public GameWorld getGameWorld() {
        return gameWorld;
    }

    public String handleCommand(String command) {
        return gameWorld.handleCommand(command);
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
                            IOExceptionMessage.append("Client connection error: ").append(e.getMessage());
                            System.out.println(IOExceptionMessage);
                        }
                    }
                    private void handleClient(Socket socket) throws IOException {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                        try {
                            String command;
                            while ((command = reader.readLine()) != null) {
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
                        } finally {
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
            if (incomingCommand != null) {
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
