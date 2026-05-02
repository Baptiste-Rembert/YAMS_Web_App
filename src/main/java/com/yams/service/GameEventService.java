package com.yams.service;

import com.yams.model.GameEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class GameEventService {

    private final SimpMessagingTemplate messagingTemplate;

    public GameEventService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendEvent(Long gameId, GameEvent event) {
        String dest = "/topic/game." + gameId + ".events";
        messagingTemplate.convertAndSend(dest, event);
    }
}
