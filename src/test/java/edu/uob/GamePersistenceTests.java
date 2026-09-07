package edu.uob;

import edu.uob.dto.CommandResponse;
import edu.uob.dto.GameSessionResponse;
import edu.uob.dto.SaveGameResponse;
import edu.uob.service.GameEngineService;
import edu.uob.service.GamePersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class GamePersistenceTests {

    @Autowired
    private GameEngineService gameEngineService;

    @Autowired
    private GamePersistenceService gamePersistenceService;

    @Test
    void testSaveAndReloadGameState() {
        // 1. Create a game session
        GameSessionResponse session = gameEngineService.createGame("Persistence Realm", "extended");
        String gameId = session.getGameId();

        // 2. Join player
        gameEngineService.joinGame(gameId, "Alice");

        // 3. Alice picks up potion & axe
        CommandResponse r1 = gameEngineService.executeCommand(gameId, "Alice", "get potion");
        assertTrue(r1.getInventory().contains("potion"));

        CommandResponse r2 = gameEngineService.executeCommand(gameId, "Alice", "get axe");
        assertTrue(r2.getInventory().contains("axe"));

        // 4. Alice unlocks trapdoor
        CommandResponse r3 = gameEngineService.executeCommand(gameId, "Alice", "unlock trapdoor");
        assertTrue(r3.getNarrative().toLowerCase().contains("unlock") || r3.getNarrative().toLowerCase().contains("cellar"));

        // 5. Alice goes to cellar
        CommandResponse r4 = gameEngineService.executeCommand(gameId, "Alice", "goto cellar");
        assertEquals("cellar", r4.getCurrentLocation());

        // 6. Save game to slot
        String slotName = "slot-checkpoint-1";
        SaveGameResponse saveResponse = gamePersistenceService.saveGame(gameId, slotName);
        assertEquals("SAVED", saveResponse.getStatus());
        assertEquals(slotName, saveResponse.getSaveSlotName());

        // 7. List saves
        List<SaveGameResponse> saves = gamePersistenceService.listSavedGames();
        assertTrue(saves.stream().anyMatch(s -> s.getSaveSlotName().equals(slotName)));

        // 8. Load game into a restored world
        GameSessionResponse loadedSession = gamePersistenceService.loadGame(slotName);
        assertEquals(gameId, loadedSession.getGameId());
        assertTrue(loadedSession.getPlayers().contains("Alice"));

        // 9. Inspect Alice's state in restored session
        CommandResponse lookResponse = gameEngineService.executeCommand(gameId, "Alice", "look");
        assertEquals("cellar", lookResponse.getCurrentLocation());
        assertTrue(lookResponse.getInventory().contains("potion"));
        assertTrue(lookResponse.getInventory().contains("axe"));

        // 10. Verify Alice can continue playing from cellar
        CommandResponse backToCabin = gameEngineService.executeCommand(gameId, "Alice", "goto cabin");
        assertEquals("cabin", backToCabin.getCurrentLocation());
    }
}
