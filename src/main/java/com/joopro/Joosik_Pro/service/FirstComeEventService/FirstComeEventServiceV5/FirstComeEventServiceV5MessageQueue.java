package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;


import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Primary
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FirstComeEventServiceV5MessageQueue implements FirstComeEventService {

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
