package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.dto.FirstComeEventParticipationDto;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceSave.SaveService;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component("v2")
@Slf4j
@RequiredArgsConstructor
//@Transactional
public class FirstComeEventServiceV2_Grafana implements FirstComeEventService {

    private static final int MAX_PARTICIPANTS = 100;
    private final SaveService saveService;
    private final MeterRegistry meterRegistry;
    private final FirstComeEventRepositoryV1 firstComeEventRepositoryV1;
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
                try {
                    saveService.saveParticipants(stockId, orderedList);
                } catch (Exception e) {
                    log.error("저장 실패: stockId={}, error={}", stockId, e.getMessage(), e);
                    meterRegistry.counter("event.save.failed", "version", "v1").increment();
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return false;
                }
            }

            return true;
        }
    }

    @Override
    public List<Long> getParticipants(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList());
    }

    @Override
    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    @Override
    public int getCurrentCount(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList()).size();
    }

    @Transactional
    @Override
    public List<FirstComeEventParticipationDto> getParticipationDtoList(Long stockId) {
        List<FirstComeEventParticipation> firstComeEventParticipation = firstComeEventRepositoryV1.findAllByStockId(stockId);
        return firstComeEventParticipation.stream()
                .map(FirstComeEventParticipationDto::of)
                .toList();
    }
}
