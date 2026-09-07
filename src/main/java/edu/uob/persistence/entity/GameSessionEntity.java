package edu.uob.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "game_sessions")
public class GameSessionEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "game_name", nullable = false)
    private String gameName;

    @Column(name = "template_name")
    private String templateName;

    @Column(name = "save_slot_name", unique = true)
    private String saveSlotName;

    @Column(name = "starting_location")
    private String startingLocation;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<PlayerStateEntity> players = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<LocationStateEntity> locations = new ArrayList<>();

    @OneToMany(mappedBy = "gameSession", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ArtefactPlacementEntity> artefacts = new ArrayList<>();

    public GameSessionEntity() {}

    public GameSessionEntity(String id, String gameName, String templateName, String saveSlotName, String startingLocation) {
        this.id = id;
        this.gameName = gameName;
        this.templateName = templateName;
        this.saveSlotName = saveSlotName;
        this.startingLocation = startingLocation;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getSaveSlotName() {
        return saveSlotName;
    }

    public void setSaveSlotName(String saveSlotName) {
        this.saveSlotName = saveSlotName;
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<PlayerStateEntity> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerStateEntity> players) {
        this.players = players;
    }

    public List<LocationStateEntity> getLocations() {
        return locations;
    }

    public void setLocations(List<LocationStateEntity> locations) {
        this.locations = locations;
    }

    public List<ArtefactPlacementEntity> getArtefacts() {
        return artefacts;
    }

    public void setArtefacts(List<ArtefactPlacementEntity> artefacts) {
        this.artefacts = artefacts;
    }
}
