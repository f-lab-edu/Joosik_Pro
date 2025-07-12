package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
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

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class FirstComeEventServiceV2_Grafana implements FirstComeEventService {

    private static final int MAX_PARTICIPANTS = 100;
    private final SaveService saveService;
    private final MeterRegistry meterRegistry;

    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        long startTime = System.nanoTime();
        meterRegistry.counter("event.participation.attempts", "version", "v2").increment(); // 총 시도 수

        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(stockId, new CopyOnWriteArrayList<>());

        Object lock = locks.computeIfAbsent(stockId, k -> new Object());

        synchronized (lock) {
            List<Long> orderedList = orderedParticipantMap.get(stockId);

            if (orderedList.size() >= MAX_PARTICIPANTS) {
                meterRegistry.counter("event.participation.full", "version", "v2").increment();
                return false;
            }

            Set<Long> participantSet = participantMap.get(stockId);

            if (participantSet.contains(memberId)) {
                meterRegistry.counter("event.participation.duplicate", "version", "v2").increment();
                return false;
            }

            participantSet.add(memberId);
            orderedList.add(memberId);
            meterRegistry.counter("event.participation.success", "version", "v2").increment();

            meterRegistry.gauge("event.current.participants",
                    List.of(io.micrometer.core.instrument.Tag.of("stockId", stockId.toString())),
                    orderedList,
                    List::size
            );
            long endTime = System.nanoTime();
            long durationNs = endTime - startTime;

            meterRegistry.timer("event.participation.time", "version", "v2")
                    .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);

            if (orderedList.size() == MAX_PARTICIPANTS) {
                meterRegistry.counter("event.save.triggered", "version", "v2").increment();
                log.info("stockId save : {}", stockId);
                saveService.saveParticipants(stockId, orderedList);
            }

            return true;
        }
    }

    public List<Long> getParticipants(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList());
    }

    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList()).size();
    }
}
