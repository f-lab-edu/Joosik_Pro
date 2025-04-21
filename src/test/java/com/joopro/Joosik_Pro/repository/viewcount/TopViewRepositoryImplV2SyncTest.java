package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.repository.PostRepository;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TopViewRepositoryImplV2Test 내에
 * synchronizeTest를 테스트 하기 위한 코드
 * 기존 TopViewRepositoryImplV2Test에서 진행했던 synchronizeTest는 updadateCacheWithDB()가
 * 별도 스레드에서 실행되면서 테스트 트랜잭션 범위 밖에서 DB에 접근하고 있음
 *
 * 테스트 트랜잭션에서 DB에서 데이터를 커밋해서 저장하지 않기 떄문에 별도의 스레드(트랜잭션)에서 접근하는 updateCahcheWithDB()
 * 에서 DB에 저장된 값을 읽어오지 못해서 값을 업데이트하지 못하는 문제 발생
 *
 * 테스트를 시작하기 전부터 DB에 값을 저장하고 나서 시작
 *
 *
 */

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sync-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class TopViewRepositoryImplV2SyncTest {

    @Autowired private TopViewRepositoryImplV2 topViewRepository;
    @Autowired private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        topViewRepository.updateCacheWithDB();
    }

    @Test
    void synchronizeTest() {
        int totalThreads = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewRepository.bulkUpdatePostViews(1L);
                    if (counter.incrementAndGet() % 50 == 0) {
                        topViewRepository.updateCacheWithDB();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        executorService.shutdown();

        assertEquals(1, topViewRepository.getPopularPosts().size());
        assertEquals(0, topViewRepository.getCache().get(1L).get()); // 캐시에는 200 count 쌓임
        assertEquals(0, topViewRepository.getReturnCache().get(1L).getViewCount());
        assertEquals(0, postRepository.findById(1L).getViewCount());
    }
}