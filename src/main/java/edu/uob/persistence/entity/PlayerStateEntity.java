package edu.uob.persistence.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "player_states")
public class PlayerStateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "health")
    private int health;

    @Column(name = "current_location")
    private String currentLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_session_id")
    private GameSessionEntity gameSession;

    @OneToMany(mappedBy = "playerState", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<InventoryItemEntity> inventory = new ArrayList<>();

    public PlayerStateEntity() {}

    public PlayerStateEntity(String username, int health, String currentLocation, GameSessionEntity gameSession) {
        this.username = username;
        this.health = health;
        this.currentLocation = currentLocation;
        this.gameSession = gameSession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public GameSessionEntity getGameSession() {
        return gameSession;
    }

    public void setGameSession(GameSessionEntity gameSession) {
        this.gameSession = gameSession;
    }

    public List<InventoryItemEntity> getInventory() {
        return inventory;
    }

    public void setInventory(List<InventoryItemEntity> inventory) {
        this.inventory = inventory;
    }
}
