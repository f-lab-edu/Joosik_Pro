package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.ChatMessage;
import com.joopro.Joosik_Pro.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat/send") // /pub/chat/send
    public void send(ChatMessage message) {
        chatService.sendMessage(message.getRoomId(), message.getContent());
    }
}

