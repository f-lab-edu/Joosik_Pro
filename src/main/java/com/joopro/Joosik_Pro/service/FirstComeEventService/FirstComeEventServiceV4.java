package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 멀티스레드를 이용하여 DB에 요청을 보내는 걸 동시에 수행하도록 수정
 * -> 동시에 DB에 접근, 각각의 요청 Blocking I/O로 접근 -> 성능 저하 유발
 * -> 여러 스레드에서 동시에 insert 날리기 보다
 * 배치 insert 사용, 배치를 사용하면 하나의 트랜잭션으로 요청 가능, 묶어서 한 번에 쓰기 가능 -> V5에서 구현
 */

@RequiredArgsConstructor
@Component
@org.springframework.transaction.annotation.Transactional(readOnly = true)
public class FirstComeEventServiceV4 {
    private final FirstComeEventRepositoryV1 eventRepositoryV1;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;
    private static final int MAX_PARTICIPANTS = 100;

    // eventId → 참여자 ID Set (중복 확인용)
    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();

    // eventId → 선착순 수 카운터
    private final ConcurrentHashMap<Long, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    public boolean tryParticipate(Long stockId, Long memberId) {
        // 참여자 Set, 순서 리스트, 카운터 초기화 (동시에 여러 쓰레드가 와도 문제없음)
        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        counterMap.putIfAbsent(stockId, new AtomicInteger(0));

        AtomicInteger counter = counterMap.get(stockId);
        if (counter.get() >= MAX_PARTICIPANTS) {
            return false;
        }

        Set<Long> participants = participantMap.get(stockId);

        // 중복 참여 먼저 확인 → 중복이면 바로 return false
        // ConcurrentHashMap의 KeySet사용 -> ConcurrentHashMap에서 put()연산 활용 -> 원자적으로 작동
        // 동시에 들어온다고 하더라도 하나가 true, 다른 건 false가 됨
        boolean isNew = participants.add(memberId);
        if (!isNew) {
            return false;
        }

        // 카운터 증가 → 현재 수가 MAX보다 작을 때만 통과
        // CAS 활용하는 AtomicInteger 활용
        int current = counter.incrementAndGet();
        if (current > MAX_PARTICIPANTS) {
            // 초과했으면 롤백
            participants.remove(memberId);
            return false;
        }

        if(current == MAX_PARTICIPANTS){
            saveToDatabase(stockId, participants);
        }

        // 순서 기록
        return true;
    }

    @Async
    public void saveToDatabase(Long stockId, Set<Long> participantsSet) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger i = new AtomicInteger();
        Stock stock = stockRepository.findStockById(stockId);

        for (Long memberId : participantsSet) {
            int order = i.incrementAndGet();
            executor.submit(() -> {
                saveParticipantTransactional(stock, memberId, order);
            });
        }

        executor.shutdown();
    }

    @Transactional
    public void saveParticipantTransactional(Stock stock, Long memberId, int order) {
        Member member = memberRepository.findOne(memberId);
        FirstComeEventParticipation participation = FirstComeEventParticipation.builder()
                .member(member)
                .stock(stock)
                .participateOrder(order)
                .build();

        eventRepositoryV1.makefirstcomeevent(participation);
    }


    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }

}
