package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 테스트 시 많은 쓰레드에서 동시에 DB에 접근하므로 DB 커넥션 풀이 모자라는 문제 발생
 * 각 요청이 @Transactional을 호출하면서 트랜잭션 커넥션 점유
 * taskExecutor는 스레드 동시에 실행하면서 커넥션 부족
 * -> unable to obtain isolated JDBC connection [HikariPool-1 - Connection is not available, request timed out after 30010ms (total=10, active=10, idle=0, waiting=0)] [n/a] 문제 발생
 *
 * ExecutorService in active state did not accept task: -> 오류 발생
 * 내부에서 비동기 처리 ExecutorService를 실행하면서 내부 Executor에 큐가 가득 찬 상황
 * 내부 Executor의 큐 사이즈 조절 -> AsyncConfig에서 QueueCapacity 높게 설정
 *
 * 애플리케이션 단에서 처리를 비동기/병렬화하여 처리량을 높이더라도, 데이터베이스 쓰기 작업에서 병목 현상이 발생하며 전체 시스템의 성능 저하를 유발 가능성 존재
 *
 */


@SpringBootTest
@SqlGroup({
        @Sql(scripts = "/multi-stock-members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS),
})
class FirstComeEventServiceV4Test {

    @Autowired
    private FirstComeEventServiceV4_2 eventService;

    @Autowired
    private FirstComeEventRepositoryV1 eventRepository;

    private final List<Long> stockIds = List.of(1L, 2L, 3L);
    private final int TOTAL_MEMBERS = 500;
    ConcurrentMap<Long, AtomicInteger> stockTryCount = new ConcurrentHashMap<>();

    @Test
    void testConcurrentParticipationAcrossMultipleStocks() throws InterruptedException {
        int threadCount = 300;
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Random random = new Random();

        for (Long stockId : stockIds) {
            stockTryCount.put(stockId, new AtomicInteger());
        }


        for (long i = 1; i <= threadCount; i++) {
            final long memberId = i;
            executor.submit(() -> {
                Long stockId = null;
                try {
                    // 무작위 stockId 선택
                    stockId = stockIds.get(random.nextInt(stockIds.size()));
                    stockTryCount.get(stockId).incrementAndGet();
                    eventService.tryParticipate(stockId, memberId);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        Thread.sleep(60000); // 비동기 저장 대기

        for (Long stockId : stockIds) {
            List<FirstComeEventParticipation> participants = eventRepository.findAllByStockId(stockId);
            System.out.println("StockID " + stockId + " 참여 인원: " + participants.size());
            assertThat(participants.size()).isLessThanOrEqualTo(100);

            // 중복 멤버 체크
            long uniqueCount = participants.stream()
                    .map(p -> p.getMember().getId())
                    .distinct()
                    .count();
            assertThat(uniqueCount).isEqualTo(participants.size());

             //participateOrder 검증
//            List<Integer> orders = participants.stream()
//                    .map(FirstComeEventParticipation::getParticipateOrder)
//                    .sorted()
//                    .collect(Collectors.toList());
//
//            for (int i = 0; i < orders.size(); i++) {
//                assertThat(orders.get(i)).isEqualTo(i + 1);
//            }

            stockTryCount.forEach((id, count) ->
                    System.out.println("StockId " + id + " 시도 횟수: " + count.get())
            );

        }

        executor.shutdown();
    }
}