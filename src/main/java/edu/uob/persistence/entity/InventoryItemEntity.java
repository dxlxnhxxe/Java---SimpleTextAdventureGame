package edu.uob.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "inventory_items")
public class InventoryItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "item_name", nullable = false)
    private String itemName;

    @Column(name = "item_description")
    private String itemDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_state_id")
    private PlayerStateEntity playerState;

    public InventoryItemEntity() {}

    public InventoryItemEntity(String itemName, String itemDescription, PlayerStateEntity playerState) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.playerState = playerState;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public PlayerStateEntity getPlayerState() {
        return playerState;
    }

    public void setPlayerState(PlayerStateEntity playerState) {
        this.playerState = playerState;
    }
}
