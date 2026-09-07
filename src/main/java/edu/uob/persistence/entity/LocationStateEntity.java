package edu.uob.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "location_states")
public class LocationStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_name", nullable = false)
    private String locationName;

    @Column(name = "description")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_paths_mapping", joinColumns = @JoinColumn(name = "location_state_id"))
    @Column(name = "target_path")
    private List<String> paths = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    private GameSessionEntity gameSession;

    public LocationStateEntity() {}

    public LocationStateEntity(String locationName, String description, List<String> paths, GameSessionEntity gameSession) {
        this.locationName = locationName;
        this.description = description;
        this.paths = (paths != null) ? new ArrayList<>(paths) : new ArrayList<>();
        this.gameSession = gameSession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getPaths() {
        return paths;
    }

    public void setPaths(List<String> paths) {
        this.paths = paths;
    }

    public GameSessionEntity getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }
}
