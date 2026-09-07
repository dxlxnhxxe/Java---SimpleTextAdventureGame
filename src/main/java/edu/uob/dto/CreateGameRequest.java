package edu.uob.dto;

public class CreateGameRequest {
    private String gameName;
    private String template;

    public CreateGameRequest() {}

    public CreateGameRequest(String gameName, String template) {
        this.gameName = gameName;
        this.template = template;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }
}
