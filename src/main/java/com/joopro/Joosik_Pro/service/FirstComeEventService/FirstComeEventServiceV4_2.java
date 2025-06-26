package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 기존 V4에서는 메서드 내부에서 @Async, @Transactional이 붙은 메서드를 호출 하는데 스프링은 내부 메서드를 호출 할 때 @Async, @Transactional 같은 AOP 기반 어노테이션 작동 안됨
 * -> DB에 저장 자체가 안되는 문제 발생
 * -> 비동기 메서드를 별도 빈 클래스로 분리 해서 외부에서 빈 호출해서 가져옴
 * -> @Async를 사용하려면 Spring의 스레드 풀 관리 하는 방법인 TaskExecutor를 통해서 실행되어야지 비동기로 가능
 * -> 그냥 원래 사용하던 ExecutorService.submit()를 사용하면 Java 기본 스레드 풀로 @Async 작동 안될 수 있음
 *
 * @Async 애노테이션은 메서드 자체를 병렬적으로 돌릴 수 있도록 하는 게 아닌 Spring이 자체적으로 TaskExecutor를 통해서 비동기로 실행시키는 것
 * 따라서 직접 TaskExecutor를 구현했다면 붙일 필요 없음.
 *
 * Set을 사용해서 내부에 order를 저장할 수 없다.
 *
 */
@Primary
@RequiredArgsConstructor
@Component
@Transactional
public class FirstComeEventServiceV4_2 implements FirstComeEventService{

    private final StockRepository stockRepository;
    private final AsyncSaveService asyncSaveService;
    private final TaskExecutor taskExecutor;

    private static final int MAX_PARTICIPANTS = 100;

    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        counterMap.putIfAbsent(stockId, new AtomicInteger(0));

        AtomicInteger counter = counterMap.get(stockId);
        if (counter.get() >= MAX_PARTICIPANTS) return false;

        Set<Long> participants = participantMap.get(stockId);
        boolean isNew = participants.add(memberId);
        if (!isNew) return false;

        int current = counter.incrementAndGet();
        if (current > MAX_PARTICIPANTS) {
            participants.remove(memberId);
            return false;
        }

        if (current == MAX_PARTICIPANTS) {
            saveToDatabaseAsync(stockId, participants);
        }

        return true;
    }

    public void saveToDatabaseAsync(Long stockId, Set<Long> participantsSet) {
        Stock stock = stockRepository.findStockById(stockId);
        AtomicInteger i = new AtomicInteger();

        for (Long memberId : participantsSet) {
            int order = i.incrementAndGet();
            taskExecutor.execute(() -> asyncSaveService.saveParticipantTransactional(stock, memberId, order));
        }
    }

    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }
}