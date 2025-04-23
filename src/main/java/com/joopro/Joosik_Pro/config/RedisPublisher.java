package com.joopro.Joosik_Pro.config;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate;

    public void sendMessage(String channelName, String message) {
        redisTemplate.convertAndSend(channelName, message);
    }

}
