package edu.uob.dto;

public class SaveGameRequest {
    private String saveSlotName;

    public SaveGameRequest() {}

    public SaveGameRequest(String saveSlotName) {
        this.saveSlotName = saveSlotName;
    }

    public String getSaveSlotName() {
        return saveSlotName;
    }

    public void setSaveSlotName(String saveSlotName) {
        this.saveSlotName = saveSlotName;
    }
}
