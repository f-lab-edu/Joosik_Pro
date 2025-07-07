package com.joopro.Joosik_Pro.service.FirstComeEventService;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class FirstComeEventServiceV5MessageQueue implements FirstComeEventService{

    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        kafkaFirstComeEventProducer.sendParticipationRequest(stockId, memberId);
        return true;
    }

    public boolean hasParticipated(Long stockId, Long memberId) {
        String key = "event:" + stockId + ":participants";
        return stringRedisTemplate.opsForZSet().score(key, memberId.toString()) != null;
    }

    public int getCurrentCount(Long stockId) {
        String key = "event:" + stockId + ":participants";
        Long count = stringRedisTemplate.opsForZSet().zCard(key);
        return count != null ? count.intValue() : 0;
    }
}
