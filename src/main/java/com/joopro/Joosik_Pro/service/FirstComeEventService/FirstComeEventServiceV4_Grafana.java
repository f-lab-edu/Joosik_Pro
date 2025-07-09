package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.StockRepository;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class FirstComeEventServiceV4_Grafana implements FirstComeEventService {

    private final StockRepository stockRepository;
    private final AsyncSaveService asyncSaveService;
    private final TaskExecutor taskExecutor;
    private final MeterRegistry meterRegistry;

    private static final int MAX_PARTICIPANTS = 100;

    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        long startTime = System.nanoTime();
        meterRegistry.counter("event.participation.attempts", "version", "v4").increment();

        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        counterMap.putIfAbsent(stockId, new AtomicInteger(0));

        AtomicInteger counter = counterMap.get(stockId);
        if (counter.get() >= MAX_PARTICIPANTS) {
            meterRegistry.counter("event.participation.full", "version", "v4").increment();
            return false;
        }

        Set<Long> participants = participantMap.get(stockId);
        boolean isNew = participants.add(memberId);
        if (!isNew) {
            meterRegistry.counter("event.participation.duplicate", "version", "v4").increment();
            return false;
        }

        int current = counter.incrementAndGet();
        if (current > MAX_PARTICIPANTS) {
            participants.remove(memberId);
            meterRegistry.counter("event.participation.full", "version", "v4").increment();
            return false;
        }

        if (current == MAX_PARTICIPANTS) {
            meterRegistry.counter("event.save.triggered", "version", "v4").increment();
            saveToDatabaseAsync(stockId, participants);
        }

        meterRegistry.counter("event.participation.success", "version", "v4").increment();

        meterRegistry.gauge("event.current.participants",
                List.of(io.micrometer.core.instrument.Tag.of("stockId", stockId.toString())),
                participants,
                Set::size
        );

        long endTime = System.nanoTime();
        long durationNs = endTime - startTime;

        meterRegistry.timer("event.participation.time", "version", "v4")
                .record(durationNs, java.util.concurrent.TimeUnit.NANOSECONDS);

        return true;
    }

    public void saveToDatabaseAsync(Long stockId, Set<Long> participantsSet) {
        Stock stock = stockRepository.findStockById(stockId);
        AtomicInteger i = new AtomicInteger();

        for (Long memberId : participantsSet) {
            int order = i.incrementAndGet();
            taskExecutor.execute(() ->
                    asyncSaveService.saveParticipantTransactional(stock, memberId, order)
            );
        }
    }

    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }
}
