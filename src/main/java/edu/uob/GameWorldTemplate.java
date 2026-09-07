package edu.uob;

import java.io.File;
import java.util.*;

/**
 * Immutable template representing a parsed STAG world definition.
 * Used to efficiently instantiate multiple independent GameWorld instances.
 */
public class GameWorldTemplate {

    private final File entitiesFile;
    private final File actionsFile;

    public GameWorldTemplate(File entitiesFile, File actionsFile) {
        this.entitiesFile = entitiesFile;
        this.actionsFile = actionsFile;
    }

    public GameWorld createNewWorld(String worldId, String worldName) {
        GameWorld world = new GameWorld(worldId, worldName);
        GameEntityParser.parseEntitiesToWorld(entitiesFile, world);
        GameActionParser.parseXMLToWorld(actionsFile, world);
        world.getAllCommandSynonyms().putAll(world.getExtendedKeyphraseSynonyms());
        return world;
    }

    public GameWorld createNewWorld() {
        return createNewWorld(UUID.randomUUID().toString(), "STAG Adventure");
    }
}
