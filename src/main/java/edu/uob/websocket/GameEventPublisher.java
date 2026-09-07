package edu.uob.websocket;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishLocationEvent(String gameId, String locationName, GameEvent event) {
        if (locationName != null && gameId != null) {
            String destination = String.format("/topic/games/%s/locations/%s", gameId, locationName.toLowerCase());
            messagingTemplate.convertAndSend(destination, event);
        }
    }

    public void publishGlobalEvent(String gameId, GameEvent event) {
        if (gameId != null) {
            String destination = String.format("/topic/games/%s/global", gameId);
            messagingTemplate.convertAndSend(destination, event);
        }
    }

    public void sendUserPrivateNotification(String username, GameEvent event) {
        if (username != null) {
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", event);
        }
    }
}
