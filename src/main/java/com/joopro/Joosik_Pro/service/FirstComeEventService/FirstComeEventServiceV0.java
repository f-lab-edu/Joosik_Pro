package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceSave.SaveService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

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
public class FirstComeEventServiceV0 implements FirstComeEventService{

    private static final int MAX_PARTICIPANTS = 100;
    private final SaveService saveService;

    // 이벤트ID → 참여자ID Set (중복 방지용)
    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();

    // 이벤트ID → 참여자 순서 리스트 (선착순 보장용)
    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(stockId, new CopyOnWriteArrayList<>());

        List<Long> orderedList = orderedParticipantMap.get(stockId);

        // 선착순 마감 확인
        if (orderedList.size() >= MAX_PARTICIPANTS) {
            return false;
        }

        Set<Long> participantSet = participantMap.get(stockId);

        // 중복 참여 확인
        if (participantSet.contains(memberId)) {
            return false;
        }

        // 참여 처리
        participantSet.add(memberId);
        orderedList.add(memberId);

        if (orderedList.size() == MAX_PARTICIPANTS){
            saveService.saveParticipants(stockId, orderedList);
        }
        return true;
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
}

