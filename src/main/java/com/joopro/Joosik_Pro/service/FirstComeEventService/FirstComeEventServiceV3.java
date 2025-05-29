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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Component
public class FirstComeEventServiceV3 {
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
    @Transactional
    private void saveToDatabase(Long stockId, Set<Long> participantsSet) {
        Stock stock = stockRepository.findStockById(stockId);
        int a = 0;
        for (Long memberId : participantsSet) {
            Member member = memberRepository.findOne(memberId);

            FirstComeEventParticipation participation = FirstComeEventParticipation.builder()
                    .member(member)
                    .stock(stock)
                    .participateOrder(a)
                    .build();
            a++;
            eventRepositoryV1.makefirstcomeevent(participation);
        }
    }


    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }

}
