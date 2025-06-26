package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * lock 대신 AtomicInteger 사용
 *
 * Set을 사용해서 내부에서 order를 저장할 수 없다.
 *
 */

@RequiredArgsConstructor
@Component
@Transactional
public class FirstComeEventServiceV3 implements FirstComeEventService{
    private final FirstComeEventRepositoryV1 eventRepositoryV1;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;
    private final SaveService saveService;
    private static final int MAX_PARTICIPANTS = 100;

    // eventId → 참여자 ID Set (중복 확인용)
    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();

    // eventId → 선착순 수 카운터
    private final ConcurrentHashMap<Long, AtomicInteger> counterMap = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        // 참여자 Set, 순서 리스트, 카운터 초기화 (동시에 여러 쓰레드가 와도 문제없음)
        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(stockId, new CopyOnWriteArrayList<>());
        counterMap.putIfAbsent(stockId, new AtomicInteger(0));

        AtomicInteger counter = counterMap.get(stockId);
        if (counter.get() >= MAX_PARTICIPANTS) {
            return false;
        }

        Set<Long> participants = participantMap.get(stockId);
        List<Long> orderedList = orderedParticipantMap.get(stockId);

        // 중복 참여 먼저 확인 → 중복이면 바로 return false
        // ConcurrentHashMap의 KeySet사용 -> ConcurrentHashMap에서 put()연산 활용 -> 원자적으로 작동
        // 동시에 들어온다고 하더라도 하나가 true, 다른 건 false가 됨
        boolean isNew = participants.add(memberId);
        if (!isNew) {
            return false;
        }

        orderedList.add(memberId);

        // 카운터 증가 → 현재 수가 MAX보다 작을 때만 통과
        // CAS 활용하는 AtomicInteger 활용
        int current = counter.incrementAndGet();
        if (current > MAX_PARTICIPANTS) {
            // 초과했으면 롤백
            participants.remove(memberId);
            return false;
        }

        if(current == MAX_PARTICIPANTS){
            saveService.saveParticipants(stockId, orderedList);
        }
        // 순서 기록
        return true;
    }

    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }

    public List<Long> getParticipants(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList());
    }

}
