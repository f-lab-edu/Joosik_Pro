package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
@Transactional
public class FirstComeEventServiceV5LuaScript implements FirstComeEventService {
    private final StringRedisTemplate redisTemplate;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;
    private final DefaultRedisScript<Long> tryParticipateScript;

    private static final int MAX_PARTICIPANTS = 100;

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        String key = "event:" + stockId + ":participants";
        String member = memberId.toString();
        String timestamp = String.valueOf(System.nanoTime());

        Long result = redisTemplate.execute(
                tryParticipateScript,
                Collections.singletonList(key),
                member, timestamp, String.valueOf(MAX_PARTICIPANTS)
        );

        if (result == null) return false;

        if (result == -1) return false;
        if (result == 0) return false;
        if (result == 1) {
            kafkaFirstComeEventProducer.saveParticipationRequest(stockId.toString());
        }

        return true;
    }
}
