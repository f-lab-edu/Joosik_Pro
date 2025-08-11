package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.dto.FirstComeEventParticipationDto;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventServiceSave.SaveService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 멀티스레드를 이용하여 DB에 요청을 보내는 걸 동시에 수행하도록 수정
 * -> 동시에 DB에 접근, 각각의 요청 Blocking I/O로 접근 -> 성능 저하 유발
 * -> 여러 스레드에서 동시에 insert 날리기 보다
 *
 * 배치 insert 사용, 배치를 사용하면 하나의 트랜잭션으로 요청 가능, 묶어서 한 번에 쓰기 가능 -> V5에서 구현
 * Set을 사용해서 내부에서 order를 저장할 수 없다.
 *
 * 메서드 내부에서 @Async, @Transactional이 붙은 메서드를 호출 하면 스프링은 내부 메서드를 호출 할 때 @Async, @Transactional 같은 AOP 기반 어노테이션 작동 안됨
 * -> DB에 저장 자체가 안되는 문제 발생
 * -> 비동기 메서드를 별도 빈 클래스로 분리 해서 외부에서 빈 호출해서 가져옴
 * -> @Async를 사용하려면 Spring의 스레드 풀 관리 하는 방법인 TaskExecutor를 통해서 실행되어야지 비동기로 가능
 * -ExecutorService.submit()를 사용하여 비동기로 실행시킬려고 하면 Java 기본 스레드 풀로 @Async 작동 안될 수 있음
 * @Async 애노테이션은 메서드 자체를 병렬적으로 돌릴 수 있도록 하는 게 아닌 Spring이 자체적으로 TaskExecutor를 통해서 비동기로 실행시키는 것, AsyncConfig 클래스에서 튜닝
 * Set을 사용해서 내부에 order를 저장할 수 없다.
 */

@Component("v4")
@RequiredArgsConstructor
@Transactional
public class FirstComeEventServiceV4 implements FirstComeEventService{
    private final SaveService saveService;
    private static final int MAX_PARTICIPANTS = 100;
    private final FirstComeEventRepositoryV1 firstComeEventRepositoryV1;
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
            saveService.asyncSaveParticipants(stockId, orderedList);
        }
        // 순서 기록
        return true;
    }

    @Override
    public boolean hasParticipated(Long stockId, Long memberId) {
        return participantMap.getOrDefault(stockId, Collections.emptySet()).contains(memberId);
    }

    @Override
    public int getCurrentCount(Long stockId) {
        return counterMap.getOrDefault(stockId, new AtomicInteger(0)).get();
    }

    @Override
    public List<Long> getParticipants(Long stockId) {
        return orderedParticipantMap.getOrDefault(stockId, Collections.emptyList());
    }

    @Override
    public List<FirstComeEventParticipationDto> getParticipationDtoList(Long stockId) {
        List<FirstComeEventParticipation> firstComeEventParticipation = firstComeEventRepositoryV1.findAllByStockId(stockId);
        return firstComeEventParticipation.stream()
                .map(FirstComeEventParticipationDto::of)
                .toList();
    }

}
