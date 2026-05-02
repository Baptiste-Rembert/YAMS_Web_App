package com.yams.service;

import com.yams.model.ChatMessage;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcast(ChatMessage message) {
        String destination = (message.getGameId() != null) ? ("/topic/game." + message.getGameId()) : "/topic/chat";
        messagingTemplate.convertAndSend(destination, message);
    }
}
