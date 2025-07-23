package com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceV5;


import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.dto.FirstComeEventParticipationDto;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service("Kafka")
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FirstComeEventServiceV5MessageQueue implements FirstComeEventService {
    private final FirstComeEventRepositoryV1 firstComeEventRepositoryV1;

    private final StringRedisTemplate stringRedisTemplate;
    private final KafkaFirstComeEventProducer kafkaFirstComeEventProducer;
    private final MeterRegistry meterRegistry;

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        log.info("stockId : {}, memberId : {}", stockId, memberId);
        meterRegistry.counter("event.participation.attempts", "version", "v5_1").increment();
        kafkaFirstComeEventProducer.sendParticipationRequest(stockId, memberId);
        return true;
    }

    @Override
    public List<Long> getParticipants(Long stockId) {
        String key = "event:" + stockId + ":participants";
        Set<String> members = stringRedisTemplate.opsForZSet().range(key, 0, -1);

        if (members == null) {
            return Collections.emptyList();
        }

        return members.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
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

    @Override
    public List<FirstComeEventParticipationDto> getParticipationDtoList(Long stockId) {
        List<FirstComeEventParticipation> firstComeEventParticipation = firstComeEventRepositoryV1.findAllByStockId(stockId);
        return firstComeEventParticipation.stream()
                .map(FirstComeEventParticipationDto::of)
                .toList();
    }
}
