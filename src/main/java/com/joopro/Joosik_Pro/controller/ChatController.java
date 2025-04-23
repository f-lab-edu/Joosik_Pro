package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.ChatMessage;
import com.joopro.Joosik_Pro.config.RedisPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@RequiredArgsConstructor
@Controller
public class ChatController {

    private final RedisPublisher redisPublisher;

    @MessageMapping("/chat/send") // /pub/chat/send
    public void send(@RequestBody ChatMessage message) {
        redisPublisher.sendMessage(message.getRoomId(), message.getContent());
    }
}

