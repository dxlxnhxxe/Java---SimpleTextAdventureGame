package edu.uob.service;

import edu.uob.*;
import edu.uob.dto.GameSessionResponse;
import edu.uob.dto.SaveGameResponse;
import edu.uob.persistence.entity.*;
import edu.uob.persistence.repository.GameSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GamePersistenceService {

    private final GameSessionRepository gameSessionRepository;
    private final GameEngineService gameEngineService;

    public GamePersistenceService(GameSessionRepository gameSessionRepository, GameEngineService gameEngineService) {
        this.gameSessionRepository = gameSessionRepository;
        this.gameEngineService = gameEngineService;
    }

    @Transactional
    public SaveGameResponse saveGame(String gameId, String saveSlotName) {
        if (saveSlotName == null || saveSlotName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Save slot name cannot be blank");
        }

        GameWorld world = gameEngineService.getGameWorld(gameId);

        String slot = saveSlotName.trim();
        GameSessionEntity entity = gameSessionRepository.findBySaveSlotName(slot)
                .orElse(new GameSessionEntity(
                        gameId,
                        world.getWorldName(),
                        "extended",
                        slot,
                        (world.getStartingLocation() != null) ? world.getStartingLocation().getName() : "cabin"
                ));

        entity.setUpdatedAt(LocalDateTime.now());
        entity.setGameName(world.getWorldName());
        entity.setStartingLocation((world.getStartingLocation() != null) ? world.getStartingLocation().getName() : "cabin");

        // Clear existing children to overwrite cleanly
        entity.getPlayers().clear();
        entity.getLocations().clear();
        entity.getArtefacts().clear();

        // 1. Snapshot locations & paths
        for (Map.Entry<String, Locations> locEntry : world.getAllLocations().entrySet()) {
            String locName = locEntry.getKey();
            Locations loc = locEntry.getValue();
            LinkedList<String> paths = world.getLocationPaths().get(locName);
            LocationStateEntity locEntity = new LocationStateEntity(
                    locName,
                    loc.getDescription(),
                    paths != null ? new ArrayList<>(paths) : Collections.emptyList(),
                    entity
            );
            entity.getLocations().add(locEntity);
        }

        // 2. Snapshot artefacts placement (across all locations + storeroom)
        for (Map.Entry<String, Map<String, Artefacts>> locArtefacts : world.getLocationWithArtefacts().entrySet()) {
            String locName = locArtefacts.getKey();
            for (Map.Entry<String, Artefacts> art : locArtefacts.getValue().entrySet()) {
                ArtefactPlacementEntity artEntity = new ArtefactPlacementEntity(
                        art.getKey(),
                        art.getValue().getDescription(),
                        locName,
                        entity
                );
                entity.getArtefacts().add(artEntity);
            }
        }

        // 3. Snapshot player states and inventory
        for (Players player : world.getAllPlayers().values()) {
            String currLoc = (player.getCurrentLocation() != null) ? player.getCurrentLocation().getName() : "cabin";
            PlayerStateEntity playerEntity = new PlayerStateEntity(
                    player.getName(),
                    player.getPlayerHealth(),
                    currLoc,
                    entity
            );
            if (player.getPlayerInventory() != null) {
                for (Artefacts invArt : player.getPlayerInventory().values()) {
                    InventoryItemEntity item = new InventoryItemEntity(
                            invArt.getName(),
                            invArt.getDescription(),
                            playerEntity
                    );
                    playerEntity.getInventory().add(item);
                }
            }
            entity.getPlayers().add(playerEntity);
        }

        gameSessionRepository.save(entity);

        return new SaveGameResponse(gameId, slot, "SAVED", entity.getUpdatedAt());
    }

    @Transactional
    public GameSessionResponse loadGame(String saveSlotName) {
        if (saveSlotName == null || saveSlotName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Save slot name cannot be blank");
        }

        GameSessionEntity entity = gameSessionRepository.findBySaveSlotName(saveSlotName.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Saved game not found for slot: " + saveSlotName));

        // Create new world template instantiation
        String activeGameId = entity.getId();
        GameWorld restoredWorld = gameEngineService.getTemplate().createNewWorld(activeGameId, entity.getGameName());

        // Restore starting location
        if (entity.getStartingLocation() != null && restoredWorld.getAllLocations().containsKey(entity.getStartingLocation())) {
            restoredWorld.setStartingLocation(restoredWorld.getAllLocations().get(entity.getStartingLocation()));
        }

        // Restore location paths
        for (LocationStateEntity locEntity : entity.getLocations()) {
            String locName = locEntity.getLocationName();
            LinkedList<String> paths = restoredWorld.getLocationPaths().computeIfAbsent(locName, k -> new LinkedList<>());
            paths.clear();
            paths.addAll(locEntity.getPaths());
        }

        // Clear default artefacts placement and rebuild from saved entity placements
        for (Map<String, Artefacts> arts : restoredWorld.getLocationWithArtefacts().values()) {
            arts.clear();
        }
        for (ArtefactPlacementEntity artEntity : entity.getArtefacts()) {
            String locName = artEntity.getLocationName();
            Artefacts artefact = new Artefacts(artEntity.getArtefactName(), artEntity.getDescription());
            restoredWorld.getLocationWithArtefacts()
                    .computeIfAbsent(locName, k -> new HashMap<>())
                    .put(artEntity.getArtefactName(), artefact);
            restoredWorld.getArtefactNames().add(artEntity.getArtefactName());
            restoredWorld.getAllEntities().put(artEntity.getArtefactName(), artefact);
        }

        // Restore players and their inventory
        restoredWorld.getAllPlayers().clear();
        restoredWorld.getLocationWithPlayers().clear();

        for (PlayerStateEntity playerEntity : entity.getPlayers()) {
            Locations playerLoc = restoredWorld.getAllLocations().get(playerEntity.getCurrentLocation());
            if (playerLoc == null) {
                playerLoc = restoredWorld.getStartingLocation();
            }
            Players player = new Players(playerEntity.getUsername(), "A brave adventurer", playerLoc, restoredWorld);
            player.setPlayerHealth(playerEntity.getHealth());

            for (InventoryItemEntity itemEntity : playerEntity.getInventory()) {
                Artefacts item = new Artefacts(itemEntity.getItemName(), itemEntity.getItemDescription());
                player.getPlayerInventory().put(itemEntity.getItemName(), item);
                restoredWorld.getArtefactNames().add(itemEntity.getItemName());
                restoredWorld.getAllEntities().put(itemEntity.getItemName(), item);
            }

            restoredWorld.getAllPlayers().put(player.getName(), player);
            if (playerLoc != null) {
                restoredWorld.getLocationWithPlayers()
                        .computeIfAbsent(playerLoc.getName(), k -> new HashMap<>())
                        .put(player.getName(), player);
            }
        }

        // Register restored world
        gameEngineService.registerGameWorld(restoredWorld);

        List<String> playerNames = new ArrayList<>(restoredWorld.getAllPlayers().keySet());
        return new GameSessionResponse(
                restoredWorld.getWorldId(),
                restoredWorld.getWorldName(),
                (restoredWorld.getStartingLocation() != null) ? restoredWorld.getStartingLocation().getName() : "cabin",
                playerNames.size(),
                playerNames
        );
    }

    public List<SaveGameResponse> listSavedGames() {
        return gameSessionRepository.findAll().stream()
                .map(entity -> new SaveGameResponse(
                        entity.getId(),
                        entity.getSaveSlotName(),
                        "PERSISTED",
                        entity.getUpdatedAt()
                ))
                .collect(Collectors.toList());
    }
}
