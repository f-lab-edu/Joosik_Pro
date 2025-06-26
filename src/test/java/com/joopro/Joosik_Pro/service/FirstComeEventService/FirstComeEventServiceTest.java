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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@SqlGroup({
        @Sql(scripts = "/multi-stock-members.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS),
})
public class FirstComeEventServiceTest {

    @Autowired
    private FirstComeEventService eventService;

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

            stockTryCount.forEach((id, count) ->
                    System.out.println("StockId " + id + " 시도 횟수: " + count.get())
            );

        }

        executor.shutdown();
    }
}
