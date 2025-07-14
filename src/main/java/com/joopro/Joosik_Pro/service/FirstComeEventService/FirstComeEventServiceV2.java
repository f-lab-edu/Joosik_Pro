package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.dto.FirstComeEventParticipationDto;
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
 * 메서드 자체에 synchornized를 걸어버리면 순서나 동시성을 맞출 필요 없는 다른 주식 ID에 대해서도 메서드 사용이 불가능해지게 됨
 *
 * 따라서 주식 ID를 사용하는 거에 락을 걸어버리면 synchornized를 주식 ID 별로 분리 가능
 *
 */

@RequiredArgsConstructor
@Component
public class FirstComeEventServiceV2 implements FirstComeEventService{
    private final FirstComeEventRepositoryV1 firstComeEventRepositoryV1;
    private final SaveService saveService;
    private static final int MAX_PARTICIPANTS = 100;

    // 이벤트ID → 참여자ID Set (중복 방지용)
    private final ConcurrentHashMap<Long, Set<Long>> participantMap = new ConcurrentHashMap<>();

    // 이벤트ID → 참여자 순서 리스트 (선착순 보장용)
    private final ConcurrentHashMap<Long, List<Long>> orderedParticipantMap = new ConcurrentHashMap<>();

    // eventId별 락 객체를 저장하기 위한 맵
    private final ConcurrentHashMap<Long, Object> locks = new ConcurrentHashMap<>();

    @Override
    public boolean tryParticipate(Long stockId, Long memberId) {
        // eventId가 동시에 접근했을 때 값이 없는 걸 확인하고 빈 set이나 list가 동시에 만들어 질 수 있지만 putIfAbsent에서
        // 키를 값과 연결시키는 작업은 원자적으로 수행되므로 동시성을 보장한다.
        participantMap.putIfAbsent(stockId, ConcurrentHashMap.newKeySet());
        orderedParticipantMap.putIfAbsent(stockId, new CopyOnWriteArrayList<>());

        // computeIfAbsent는 키에 대해 값이 없으면 새로운 값 생성한 뒤에 값 반환 -> Atomic하게 수행
        Object lock = locks.computeIfAbsent(stockId, k -> new Object());

        // 락 이용
        synchronized (lock) {
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

            // 이 부분을 외부로 분리하면 안되나?
            // -> 바로 위 부분을 끝내고 바깥에서 값을 넣을려고 할 때(아래 부분을 외부로 분리했을 때)
            // 그 전에 어떤 스레드가 중복 참여 했는지 확인 했을 때 안되있음
            // 2번 참여 가능 -> 문제 상황 발생
            participantSet.add(memberId);
            orderedList.add(memberId);

            if (orderedList.size() == MAX_PARTICIPANTS){
                saveService.saveParticipants(stockId, orderedList);
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

    @Override
    public List<FirstComeEventParticipationDto> getParticipationDtoList(Long stockId) {
        List<FirstComeEventParticipation> firstComeEventParticipation = firstComeEventRepositoryV1.findAllByStockId(stockId);
        return firstComeEventParticipation.stream()
                .map(FirstComeEventParticipationDto::of)
                .toList();
    }

}