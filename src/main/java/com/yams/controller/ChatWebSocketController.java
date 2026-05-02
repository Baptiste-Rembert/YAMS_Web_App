package com.yams.controller;

import com.yams.model.ChatMessage;
import com.yams.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWebSocketController {

    private final ChatService chatService;

    public ChatWebSocketController(ChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send") // clients send to /app/chat.send
    public void sendMessage(@Payload ChatMessage message) {
        chatService.broadcast(message);
    }

    @MessageMapping("/chat.join")
    public void join(@Payload ChatMessage message) {
        // broadcast join notification or handle presence
        chatService.broadcast(message);
    }
}
