package edu.uob.websocket;

import edu.uob.dto.CommandRequest;
import edu.uob.dto.CommandResponse;
import edu.uob.service.GameEngineService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    private final GameEngineService gameEngineService;
    private final GameEventPublisher eventPublisher;

    public GameWebSocketController(GameEngineService gameEngineService, GameEventPublisher eventPublisher) {
        this.gameEngineService = gameEngineService;
        this.eventPublisher = eventPublisher;
    }

    @MessageMapping("/games/{gameId}/chat")
    public void handleChatMessage(@DestinationVariable("gameId") String gameId, @Payload GameEvent chatEvent) {
        chatEvent.setGameId(gameId);
        chatEvent.setEventType(GameEvent.EventType.CHAT_MESSAGE);

        if (chatEvent.getLocation() != null && !chatEvent.getLocation().isBlank()) {
            eventPublisher.publishLocationEvent(gameId, chatEvent.getLocation(), chatEvent);
        } else {
            eventPublisher.publishGlobalEvent(gameId, chatEvent);
        }
    }

    @MessageMapping("/games/{gameId}/command")
    @SendToUser("/queue/notifications")
    public CommandResponse handleWebSocketCommand(
            @DestinationVariable("gameId") String gameId,
            @Payload CommandRequest request) {
        return gameEngineService.executeCommand(gameId, request.getPlayerName(), request.getCommand());
    }
}
