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

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sync-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
class TopViewRepositoryImplV2SyncTest {

    private static final Logger log = LoggerFactory.getLogger(TopViewRepositoryImplV2SyncTest.class);
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
        assertEquals(totalThreads, topViewRepository.getCache().get(1L).get()); // 캐시에는 200 count 쌓임
        assertEquals(totalThreads, topViewRepository.getReturnCache().get(1L).getViewCount());
        assertEquals(totalThreads, postRepository.findById(1L).getViewCount());
    }
}