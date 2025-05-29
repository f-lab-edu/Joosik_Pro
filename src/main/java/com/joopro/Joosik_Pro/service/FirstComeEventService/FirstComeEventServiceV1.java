package com.joopro.Joosik_Pro.service.FirstComeEventService;
import java.util.*;
import java.util.concurrent.*;

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

/**
 * ConcurrentHashSet.netKeySet()는 스레드 안전하다. -> 내부적으로 동시성 처리, 분할 락 사용한다.
 * 일반 락(ex. Collections.synchronizedSet(new HashSet<>())) 은 모든 작업에 대해서 하나의 락만을 사용하므로 자료구조에 하나의 스레드만 접근이 가능하지만
 * 분할 락은 내부 데이터를 나누기 때문에 서로 다른 key에 대해서는 여러 스레드에서 접근이 안전하게 가능하다.
 *
 * CopyonWriteArrayList는 스레드 안전하다. -> 데이터 수정 시 새로운 배열을 복사해서 만들고, 복사본에 변경 적, 읽기 작업은 락 없이 가능
 * 따라서 읽기 작업은 매우 빠르지만 쓰기 작업은 매우 느리다
 *
 * -> 위 2개로 스레드 안전하게 작업할 수 있는가?
 *
 * 메서드에 synchronized를 걸어 다른 스레드가 사용중 일 때 메서드 사용 원전 봉쇄
 * 멀티 스레드 환경에서 tryParticipate 메서드로 인해서 병목이 생길 수 있다.
 *
 */

@RequiredArgsConstructor
@Component
public class FirstComeEventServiceV1 {

    private static final int MAX_PARTICIPANTS = 100;
    private final FirstComeEventRepositoryV1 eventRepositoryV1;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;

    // 이벤트ID → 참여자ID Set (중복 방지용)
    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();

    // 이벤트ID → 참여자 순서 리스트 (선착순 보장용)
    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();

    public synchronized boolean tryParticipate(Long eventId, Long memberId) {
        participantMap.putIfAbsent(eventId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(eventId, new CopyOnWriteArrayList<>());

        List<Long> orderedList = orderedParticipantMap.get(eventId);

        // 선착순 마감 확인
        if (orderedList.size() >= MAX_PARTICIPANTS) {
            return false;
        }

        Set<Long> participantSet = participantMap.get(eventId);

        // 중복 참여 확인
        if (participantSet.contains(memberId)) {
            return false;
        }

        // 참여 처리
        participantSet.add(memberId);
        orderedList.add(memberId);

        if (orderedList.size() == MAX_PARTICIPANTS){
            saveToDatabase(eventId, orderedList);
        }
        return true;
    }

    @Async
    @Transactional
    private void saveToDatabase(Long eventId, List<Long> orderedList) {
        Stock stock = stockRepository.findStockById(eventId);
        int a = 0;
        for (Long memberId : orderedList) {
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

    public List<Long> getParticipants(Long eventId) {
        return orderedParticipantMap.getOrDefault(eventId, Collections.emptyList());
    }

    public boolean hasParticipated(Long eventId, Long memberId) {
        return participantMap.getOrDefault(eventId, Collections.emptySet()).contains(memberId);
    }

    public int getCurrentCount(Long eventId) {
        return orderedParticipantMap.getOrDefault(eventId, Collections.emptyList()).size();
    }
}

