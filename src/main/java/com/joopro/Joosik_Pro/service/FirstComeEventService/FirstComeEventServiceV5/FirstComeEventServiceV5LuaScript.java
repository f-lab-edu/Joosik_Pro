package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;

import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
@Transactional
public class FirstComeEventServiceV5LuaScript implements FirstComeEventService {
    private final StringRedisTemplate redisTemplate;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;
    private final DefaultRedisScript<Long> tryParticipateScript;
    private final MeterRegistry meterRegistry;

    private static final int MAX_PARTICIPANTS = 100;

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        long startTime = System.nanoTime();
        meterRegistry.counter("event.participation.attempts", "version", "v5_2").increment();
        String key = "event:" + stockId + ":participants";
        String member = memberId.toString();
        String timestamp = String.valueOf(System.nanoTime());

        Long result = redisTemplate.execute(
                tryParticipateScript,
                Collections.singletonList(key),
                member, timestamp, String.valueOf(MAX_PARTICIPANTS)
        );

        if (result == null || result == -1) {
            meterRegistry.counter("event.participation.duplicate", "version", "v5_2").increment();
            return false;
        }

        if (result == 0) {
            meterRegistry.counter("event.participation.full", "version", "v5_2").increment();
            return false;
        }
        if (result == 1) {
            meterRegistry.counter("event.save.triggered", "version", "v5_2").increment();
            kafkaFirstComeEventProducer.saveParticipationRequest(stockId.toString());
        }
        meterRegistry.counter("event.participation.success", "version", "v5_2").increment();

        // 실시간 참여자 수 gauge
        Long count = redisTemplate.opsForZSet().zCard(key);
        meterRegistry.gauge("event.current.participants",
                List.of(Tag.of("stockId", stockId.toString())),
                count != null ? count : 0
        );

        long duration = System.nanoTime() - startTime;
        meterRegistry.timer("event.participation.time", "version", "v5_2")
                .record(duration, TimeUnit.NANOSECONDS);

        return true;
    }
}
