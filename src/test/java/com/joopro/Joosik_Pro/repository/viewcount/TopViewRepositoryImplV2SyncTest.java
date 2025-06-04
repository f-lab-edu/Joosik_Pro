package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
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
 * DB 캐시 업데이트와 @Transactional 적용에 대한 고민
 * 캐시 업데이트 코드에 @Transactional이 누락
 * Repositroy 단에 바로 @Transactional을 붙이는 방식과 Service단에서 @Transactional을 호출하는 방식 중 고민
 * Service 단에서 호출 시, 노출할 필요가 없는 캐시 업데이트 코드가 public으로 열려야 하는 문제 발생
 * Repository 단에 @Transactional을 바로 넣는 것은 구조적으로 부자연스러움
 *
 * 테스트 코드 시 private 메서드에 대한 고민
 * private 메서드의 테스트 작성이 구조적으로 옳지 않다는 문제 인식
 *
 * 결론
 * @PostConstruct 어노테이션을 사용해 Init() 메서드에 캐시 업데이트 코드를 작성
 * Init() 메서드를 public으로 열어 호출 가능하게 함
 * 필요한 부분만 테스트 가능하게 수정
 */

@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sync-test-data.sql", executionPhase = ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = "/sync-test-cleanup.sql", executionPhase = ExecutionPhase.AFTER_TEST_CLASS)
class TopViewRepositoryImplV2SyncTest {

    @Autowired private TopViewRepositoryImplV2 topViewRepository;
    @Autowired private PostRepository postRepository;
    @BeforeEach
    void setUp() throws Exception {
        LinkedHashMap<Long, Post> returnCache = accessReturnCacheByReflection();
        Map<Long, AtomicInteger> cache = accessCacheByReflection();
        returnCache.clear();
        cache.clear();
        topViewRepository.updateCacheWithDB();
    }

    @Test
    void synchronizeTest() throws Exception {
        int totalThreads = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewRepository.bulkUpdatePostViews(1L);
                    if (counter.incrementAndGet() % 50 == 0) {
                        topViewRepository.init();
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

        topViewRepository.init();

        LinkedHashMap<Long, Post> returnCache = accessReturnCacheByReflection();
        Map<Long, AtomicInteger> cache = accessCacheByReflection();

        assertEquals(1, topViewRepository.getPopularPosts().size());
        assertEquals(200, cache.get(1L).get());
        assertEquals(200, returnCache.get(1L).getViewCount());
        assertEquals(200, postRepository.findById(1L).getViewCount());
    }

    @SuppressWarnings("unchecked")
    private Map<Long, AtomicInteger> accessCacheByReflection() throws Exception {
        var method = TopViewRepositoryImplV2.class.getDeclaredMethod("getCacheForTest");
        method.setAccessible(true);
        return (Map<Long, AtomicInteger>) method.invoke(topViewRepository);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<Long, Post> accessReturnCacheByReflection() throws Exception {
        var method = TopViewRepositoryImplV2.class.getDeclaredMethod("getReturnCacheForTest");
        method.setAccessible(true);
        return (LinkedHashMap<Long, Post>) method.invoke(topViewRepository);
    }

}