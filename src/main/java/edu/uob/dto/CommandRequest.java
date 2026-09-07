package edu.uob.dto;

import java.util.List;

public class CommandRequest {
    private String playerName;
    private String command;

    public CommandRequest() {}

    public CommandRequest(String playerName, String command) {
        this.playerName = playerName;
        this.command = command;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
