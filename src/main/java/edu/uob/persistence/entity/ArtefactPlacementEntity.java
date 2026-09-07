package edu.uob.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "artefact_placements")
public class ArtefactPlacementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artefact_name", nullable = false)
    private String artefactName;

    @Column(name = "description")
    private String description;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    private GameSessionEntity gameSession;

    public ArtefactPlacementEntity() {}

    public ArtefactPlacementEntity(String artefactName, String description, String locationName, GameSessionEntity gameSession) {
        this.artefactName = artefactName;
        this.description = description;
        this.locationName = locationName;
        this.gameSession = gameSession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArtefactName() {
        return artefactName;
    }

    public void setArtefactName(String artefactName) {
        this.artefactName = artefactName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public GameSessionEntity getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }
}
