package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceSave.SaveService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class FirstComeEventServiceV3_Grafana implements FirstComeEventService {

    private final SaveService saveService;
    private final MeterRegistry meterRegistry;

    private static final int MAX_PARTICIPANTS = 100;

    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> counterMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        log.info("stockId : {}, memberId : {}", stockId, memberId);
        long startTime = System.nanoTime();
        meterRegistry.counter("event.participation.attempts", "version", "v3").increment(); // 시도 수

        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(stockId, new CopyOnWriteArrayList<>());
        counterMap.putIfAbsent(stockId, new AtomicInteger(0));

        AtomicInteger counter = counterMap.get(stockId);
        if (counter.get() >= MAX_PARTICIPANTS) {
            meterRegistry.counter("event.participation.full", "version", "v3").increment();
            return false;
        }

        Set<Long> participants = participantMap.get(stockId);
        List<Long> orderedList = orderedParticipantMap.get(stockId);

        boolean isNew = participants.add(memberId);
        if (!isNew) {
            meterRegistry.counter("event.participation.duplicate", "version", "v3").increment();
            return false;
        }

        orderedList.add(memberId);
        int current = counter.incrementAndGet();

        if (current > MAX_PARTICIPANTS) {
            participants.remove(memberId); // 롤백
            meterRegistry.counter("event.participation.full", "version", "v3").increment();
            return false;
        }

        meterRegistry.counter("event.participation.success", "version", "v3").increment();

        // 실시간 참여자 수 gauge
        meterRegistry.gauge("event.current.participants",
                List.of(io.micrometer.core.instrument.Tag.of("stockId", stockId.toString())),
                orderedList,
                List::size
        );

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;

        meterRegistry.timer("event.participation.time", "version", "v3")
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);

        if (current == MAX_PARTICIPANTS) {
            meterRegistry.counter("event.save.triggered", "version", "v3").increment();
            log.info("stockId save : {}", stockId);
            saveService.saveParticipants(stockId, orderedList);
        }

        return true;
    }

    @Override
    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    @Override
    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }

    @Override
    public List<Long> getParticipants(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList());
    }
}

