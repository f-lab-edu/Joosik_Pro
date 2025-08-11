package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.dto.PostDtoResponse;
import com.joopro.Joosik_Pro.service.PostService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@SqlGroup({
        @Sql(scripts = "/redis-stock.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "/redis-stock-cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
public class RedisTopViewRepositoryImpl_FinalSyncTest {
    @Autowired private RedissonClient redissonClient;
    @Autowired private RedisTopViewRepositoryImpl_Final topViewRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;
    @Autowired private PostService postService;

    // 저장소 클래스의 LOCK_KEY와 반드시 동일해야 함
    private static final String LOCK_KEY = "topViewReadWriteLock";

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        topViewRepository.updateCacheWithDBAutomatically();
    }

    @AfterEach
    void clearCache() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("writeLock을 테스트가 직접 선점하면, returnPost(read)가 대기한다")
    @Test
    void write잠금_중_read는_대기한다() throws Exception {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        RReadWriteLock rw = redissonClient.getReadWriteLock(LOCK_KEY);

        // 1) 테스트가 writeLock을 먼저 잡는다 (대기 1초, 임계구역 30초)
        boolean acquired = rw.writeLock().tryLock(1, 30, TimeUnit.SECONDS);
        assertThat(acquired).as("테스트가 writeLock을 선점해야 함").isTrue();

        Future<PostDtoResponse> future = null;
        try {
            // 2) write 잠금이 걸려있는 상태에서 read 시도
            future = executorService.submit(() -> topViewRepository.returnPost(1L));

            // read가 큐에 들어갈 시간을 잠깐 줌
            Thread.sleep(150);

            // 3) write 잠금 유지 중에는 read가 완료되면 안 됨
            assertThat(future.isDone())
                    .as("write 잠금 동안에는 returnPost가 완료되면 안 됨")
                    .isFalse();

        } finally {
            // 4) write 해제
            if (rw.writeLock().isLocked() && rw.writeLock().isHeldByCurrentThread()) {
                rw.writeLock().unlock();
            }
        }

        // 5) 잠금 해제 후에는 빠르게 진행되어야 함
        PostDtoResponse postDtoResponse = future.get(1, TimeUnit.SECONDS);
        assertThat(postDtoResponse).isNotNull();

        executorService.shutdownNow();
    }


    @DisplayName("readLock은 여러 쓰레드가 동시에 접근 가능함을 확인")
    @Test
    void 여러_returnPost_동시_실행_가능_확인() throws Exception {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Post post = postService.findPostByPostId(1L);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    topViewRepository.returnPost(post.getId());
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

        executor.shutdown();

        Double score = redisTemplate.opsForZSet().score("popularPostsZSet", String.valueOf(post.getId()));
        assertThat(score).isEqualTo((double) 100L);
    }

}
