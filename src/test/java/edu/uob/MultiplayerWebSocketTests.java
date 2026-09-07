package edu.uob;

import edu.uob.dto.CommandResponse;
import edu.uob.dto.GameSessionResponse;
import edu.uob.service.GameEngineService;
import edu.uob.websocket.GameEvent;
import edu.uob.websocket.GameEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class MultiplayerWebSocketTests {

    @Autowired
    private GameEngineService gameEngineService;

    @Autowired
    private GameEventPublisher gameEventPublisher;

    @Test
    void testMultiplayerLocationEventsFlow() {
        GameSessionResponse session = gameEngineService.createGame("Multiplayer Realm", "extended");
        String gameId = session.getGameId();

        // Player A joins
        gameEngineService.joinGame(gameId, "Alice");

        // Player B joins
        gameEngineService.joinGame(gameId, "Bob");

        // Player A looks: should see Bob in the room
        CommandResponse lookA = gameEngineService.executeCommand(gameId, "Alice", "look");
        assertTrue(lookA.getOtherPlayersInLocation().contains("Bob"), "Alice should see Bob in cabin");
        assertTrue(lookA.getNarrative().contains("Bob"), "Narrative should list other player Bob");

        // Player B moves to forest
        CommandResponse moveB = gameEngineService.executeCommand(gameId, "Bob", "goto forest");
        assertEquals("forest", moveB.getCurrentLocation());

        // Player A looks again: Bob is no longer in cabin
        CommandResponse lookA2 = gameEngineService.executeCommand(gameId, "Alice", "look");
        assertFalse(lookA2.getOtherPlayersInLocation().contains("Bob"), "Alice should no longer see Bob in cabin");

        // Player A moves to forest: now sees Bob again
        CommandResponse moveA = gameEngineService.executeCommand(gameId, "Alice", "goto forest");
        assertEquals("forest", moveA.getCurrentLocation());
        assertTrue(moveA.getOtherPlayersInLocation().contains("Bob"), "Alice should see Bob in forest");

        // Event publisher checks (doesn't throw exceptions)
        assertDoesNotThrow(() -> {
            gameEventPublisher.publishLocationEvent(gameId, "forest", new GameEvent(
                    GameEvent.EventType.CHAT_MESSAGE, gameId, "Alice", "forest", "Hello Bob!"
            ));
            gameEventPublisher.publishGlobalEvent(gameId, new GameEvent(
                    GameEvent.EventType.GLOBAL_ANNOUNCEMENT, gameId, "SYSTEM", "", "Server reboot in 5m"
            ));
        });
    }
}
