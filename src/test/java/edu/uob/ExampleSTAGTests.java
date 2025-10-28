package edu.uob;

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.nio.file.Paths;
import java.io.IOException;
import java.time.Duration;

class ExampleSTAGTests {

  private GameServer server;

  // Create a new server _before_ every @Test
  @BeforeEach
  void setup() {
      File entitiesFile = Paths.get("config" + File.separator + "extended-entities.dot").toAbsolutePath().toFile();
      File actionsFile = Paths.get("config" + File.separator + "extended-actions.xml").toAbsolutePath().toFile();
      server = new GameServer(entitiesFile, actionsFile);
  }

  String sendCommandToServer(String command) {
      // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
      return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
      "Server took too long to respond (probably stuck in an infinite loop)");
  }

  // A lot of tests will probably check the game state using 'look' - so we better make sure 'look' works well !
  @Test
  void testLook() {
    String response = sendCommandToServer("simon: look");
    response = response.toLowerCase();
    assertTrue(response.contains("cabin"), "Did not see the name of the current room in response to look");
      System.out.println("LOOK RESPONSE: " + response);
    assertTrue(response.contains("log cabin"), "Did not see a description of the room in response to look");
    assertTrue(response.contains("magic potion"), "Did not see a description of artifacts in response to look");
    assertTrue(response.contains("wooden trapdoor"), "Did not see description of furniture in response to look");
    assertTrue(response.contains("forest"), "Did not see available paths in response to look");
  }

  // Test that we can pick something up and that it appears in our inventory
  @Test
  void testGet()
  {
      String response;
      sendCommandToServer("simon: get potion");
      response = sendCommandToServer("simon: inv");
      response = response.toLowerCase();
      assertTrue(response.contains("potion"), "Did not see the potion in the inventory after an attempt was made to get it");
      response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertFalse(response.contains("potion"), "Potion is still present in the room after an attempt was made to get it");
  }

  // Test that we can goto a different location (we won't get very far if we can't move around the game !)
  @Test
  void testGoto()
  {
      sendCommandToServer("simon: goto forest");
      String response = sendCommandToServer("simon: look");
      response = response.toLowerCase();
      assertTrue(response.contains("key"), "Failed attempt to use 'goto' command to move to the forest - there is no key in the current location");
  }

  // Add more unit tests or integration tests here.
  @Test
  void testPartialCommandChopTree() {
      String response = sendCommandToServer("simon: chop tree").toLowerCase();
      assertTrue(response.contains("chop") || response.contains("axe") || response.contains("tree"),
              "Interpreter could not handle partial command 'chop tree'");
  }

    @Test
    void testPartialActionCommandWorks() {
        sendCommandToServer("simon: get axe");
        String response = sendCommandToServer("simon: chop").toLowerCase();
        assertTrue(response.contains("tree") || response.contains("chopped"),
                "Partial command 'chop' did not trigger expected action");
    }

    @Test
    void testMultipleEntitiesConsumed() {
        sendCommandToServer("simon: get axe");
        sendCommandToServer("simon: get plank");
        String response = sendCommandToServer("simon: build bridge").toLowerCase();
        assertTrue(response.contains("bridge"), "Bridge was not built");
        response = sendCommandToServer("simon: inv").toLowerCase();
        assertFalse(response.contains("axe") && response.contains("plank"),
                "Items were not consumed after building bridge");
    }

    @Test
    void testHealthReducedAfterFight() {
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: fight pixie");
        String response = sendCommandToServer("simon: health").toLowerCase();
        assertTrue(response.contains("health") || response.contains("reduced"),
                "Health was not reduced after fighting pixie");
    }

    @Test
    void testActionProducesNarration() {
        String response = sendCommandToServer("simon: open trapdoor").toLowerCase();
        assertTrue(response.contains("open") || response.contains("trapdoor"),
                "No suitable narration returned after performing action");
    }

    @Test
    void testUnlockTrapdoorCreatesPath() {
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        String response = sendCommandToServer("simon: look").toLowerCase();
        assertTrue(response.contains("cellar"), "Unlocking trapdoor did not create new path to cellar");
    }

    @Test
    void testInterpreterHandlesPoliteExtraWords() {
        sendCommandToServer("simon: get axe");
        String response = sendCommandToServer("simon: could you please chop down the tree with the axe").toLowerCase();
        assertTrue(response.contains("tree") || response.contains("chopped"),
                "Interpreter did not handle extra words correctly");
    }

    @Test
    void testConsumePotion() {
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: drink potion");
        String response = sendCommandToServer("simon: inv").toLowerCase();
        assertFalse(response.contains("potion"), "Potion not consumed after drinking");
    }

    @Test
    void testCaseInsensitiveCommands() {
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: DRiNK potion");
        String response = sendCommandToServer("simon: inv").toLowerCase();
        assertFalse(response.contains("potion"), "Potion was not consumed when using mixed-case command");
    }

    @Test
    void testChangedWordOrder() {
        sendCommandToServer("simon: get axe");
        String response = sendCommandToServer("simon: tree chop").toLowerCase();
        assertTrue(response.contains("tree") || response.contains("chopped"),
                "Interpreter could not handle changed word order");
    }

    @Test
    void testSummonProducesCharacter() {
        String response = sendCommandToServer("simon: summon lumberjack").toLowerCase();
        assertTrue(response.contains("lumberjack"), "Character not produced when summoning lumberjack");
    }

    @Test
    void testHealthIncreasedByPositiveAction() {
        sendCommandToServer("simon: get potion");
        sendCommandToServer("simon: drink potion");
        String response = sendCommandToServer("simon: health").toLowerCase();
        assertTrue(response.contains("increased") || response.contains("higher"),
                "Health did not increase after drinking potion");
    }

    @Test
    void testInventoryConsumedAfterPlankAction() {
        sendCommandToServer("simon: get plank");
        sendCommandToServer("simon: build bridge");
        String response = sendCommandToServer("simon: inv").toLowerCase();
        assertFalse(response.contains("plank"), "Plank was not consumed after action");
    }

    @Test
    void testLoseInventoryWhenHealthZero() {
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: fight pixie");
        sendCommandToServer("simon: fight pixie"); // repeat to reduce health
        String response = sendCommandToServer("simon: inv").toLowerCase();
        assertFalse(response.contains("axe") || response.contains("potion"),
                "Inventory was not cleared after player lost all health");
    }

    @Test
    void testMultipleActionTriggers() {
        sendCommandToServer("simon: open door");
        String response1 = sendCommandToServer("simon: open potion").toLowerCase();
        assertTrue(response1.contains("open") || response1.contains("door") || response1.contains("potion"),
                "Did not handle multiple actions with same trigger correctly");
    }

    @Test
    void testInterpreterHandlesExtraWordsInCommand() {
        String response = sendCommandToServer("simon: please open the wooden trapdoor now").toLowerCase();
        assertTrue(response.contains("trapdoor"), "Interpreter failed when extra words added to command");
    }

    @Test
    void testSpacesInTriggerPhrases() {
        String response = sendCommandToServer("simon: pull lever down").toLowerCase();
        assertTrue(response.contains("lever"), "Interpreter failed to cope with spaces in trigger phrase");
    }

    @Test
    void testFullMarkingGameRunthrough() {
        // Simulate simplified sequence: get key, unlock trapdoor, goto cellar, get gold
        sendCommandToServer("simon: goto forest");
        sendCommandToServer("simon: get key");
        sendCommandToServer("simon: goto cabin");
        sendCommandToServer("simon: unlock trapdoor");
        sendCommandToServer("simon: goto cellar");
        String response = sendCommandToServer("simon: get gold").toLowerCase();
        assertTrue(response.contains("gold"), "Could not complete marking game to retrieve gold");
    }

    @Test
    void testValidLocationSubjectForAction() {
        String response = sendCommandToServer("simon: open cabin").toLowerCase();
        assertTrue(response.contains("cannot") || response.contains("invalid") || response.contains("cabin"),
                "Location incorrectly treated as valid subject for action");
    }

}
