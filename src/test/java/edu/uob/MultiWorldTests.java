package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class MultiWorldTests {

    private GameWorldTemplate template;

    @BeforeEach
    void setUp() {
        File entitiesFile = Paths.get("config", "extended-entities.dot").toAbsolutePath().toFile();
        File actionsFile = Paths.get("config", "extended-actions.xml").toAbsolutePath().toFile();
        template = new GameWorldTemplate(entitiesFile, actionsFile);
    }

    @Test
    void testConcurrentWorldIsolation() {
        GameWorld world1 = template.createNewWorld("world-1", "Realm Alpha");
        GameWorld world2 = template.createNewWorld("world-2", "Realm Beta");

        // Player Alice in world 1 picks up potion and axe
        String r1 = world1.handleCommand("alice: get potion");
        assertTrue(r1.toLowerCase().contains("potion"));
        String r2 = world1.handleCommand("alice: get axe");
        assertTrue(r2.toLowerCase().contains("axe"));

        // In world 1, cabin should no longer have potion
        String look1 = world1.handleCommand("alice: look").toLowerCase();
        assertFalse(look1.contains("potion : a bottle of magic potion"));

        // In world 2, Player Bob looks around: potion and axe should still be present in the cabin
        String look2 = world2.handleCommand("bob: look").toLowerCase();
        assertTrue(look2.contains("potion : a bottle of magic potion"), "World 2 cabin should still contain potion");
        assertTrue(look2.contains("axe : a razor sharp axe"), "World 2 cabin should still contain axe");

        // Bob in world 2 picks up potion
        String r3 = world2.handleCommand("bob: get potion");
        assertTrue(r3.toLowerCase().contains("potion"));

        // Bob's inventory in world 2 has potion
        String inv2 = world2.handleCommand("bob: inv").toLowerCase();
        assertTrue(inv2.contains("potion"));

        // Alice's inventory in world 1 has axe and potion
        String inv1 = world1.handleCommand("alice: inv").toLowerCase();
        assertTrue(inv1.contains("axe"));
        assertTrue(inv1.contains("potion"));

        // Bob cannot see Alice in World 2
        assertFalse(look2.contains("alice"));
    }

    @Test
    void testIndependentExtendedActions() {
        GameWorld world1 = template.createNewWorld("world-1", "Realm Alpha");
        GameWorld world2 = template.createNewWorld("world-2", "Realm Beta");

        // In world 1, unlock trapdoor
        world1.handleCommand("alice: unlock trapdoor");
        String look1 = world1.handleCommand("alice: look").toLowerCase();
        assertTrue(look1.contains("cellar"), "Cellar path should exist in world 1");

        // In world 2, trapdoor should not be unlocked yet
        String look2 = world2.handleCommand("bob: look").toLowerCase();
        assertFalse(look2.contains("cellar"), "Cellar path should not exist in world 2 yet");
    }
}
