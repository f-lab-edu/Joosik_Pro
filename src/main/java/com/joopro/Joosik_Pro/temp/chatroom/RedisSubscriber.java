package com.joopro.Joosik_Pro.temp.chatroom;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(pattern);
        String msg = new String(message.getBody());

        // WebSocket을 통해 프론트에 전송
        messagingTemplate.convertAndSend("/sub/chat/" + channel, msg);
    }
}

