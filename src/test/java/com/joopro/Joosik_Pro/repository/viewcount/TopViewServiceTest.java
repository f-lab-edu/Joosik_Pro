package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.service.TopViewService.TopViewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * TopViewRepositoryImplV2SyncTest에서 언급했듯이 여러 스레드 테스트를 할 때는 @Transactional 적용 때문에 Service 레이어에서 통합테스트가 되어야 한다.
 * 동시성 테스트는 특정 로직구간을 검증하기 위해서 작성했다기 보다는 사용자가 많이 몰리는 경우를 가정한 것이기에 Service 레이어의 메서드를 호출하면서 테스트가 진행되어야 한다.
 *
 * 동시성 테스트 즉, 한 스레드가 조회수를 DB에 업데이트 시킬 때 다른 스레드가 조회수를 올리면 조회수가 제대로 올라갈까 하는 동시성 테스트 실행
 * TopViewRepositoryImplV3에 이미 존재하고 있는 @Scheduled를 사용하기 위해서 시간 타임을 빨리 돌리기 위해서 application.properties 파일 따로 지정
 * @Scheduled는 처음 시작할 때는 작동하지 않는다.
 *
 * -> @Scheduled는 테스트 환경에서는 작동하지 않는다. -> 중간에 직접 호출하는 방식 활용
 *
 */
@TestPropertySource(locations = "classpath:application-test.properties")
@Sql(scripts = "/sync-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@SpringBootTest
public class TopViewServiceTest {

    @Autowired TopViewService topViewService;
    @Autowired TopViewRepositoryImplV3 topViewRepositoryImplV3;
    @Autowired TopViewSchedulerService topViewSchedulerService;
    @Autowired
    PostRepository postRepository;


    @Test
    void synchronizeTest() {
        int totalThreads = 500;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger counter = new AtomicInteger();

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewService.returnPost(1L);
                    int current = counter.incrementAndGet();
                    if (current == 250) { // 250번째 호출 중에 스케줄러 실행
                        topViewSchedulerService.updateCacheWithDBAutomatically();
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

        topViewRepositoryImplV3.init();

        assertEquals(1, topViewService.getPopularArticles().size());
//        assertEquals(500, TopViewRepositoryImplV3.getTempViewCount().get(1L));
        assertEquals(500, postRepository.findById(1L).getViewCount());

    }

    /**
     * 처음에 topViewRepositoryImplV3.init()을 하지 않고 @PostConstruct 안에 있는 init을 사용하면 된다고 생각했지만
     * postConstruct는 처음에 스프링부트(빈)이 뜰 때 작동하므로 @SQL이 적용되기 이전 시점에 작동함 따라서 cache에는 아무꺼도 들어가지 않음
     *
     * 그러므로 테스트를 띄우고 난 뒤 @SQL이 작동하고 데이터가 DB안에 들어간 뒤 topViewRepositoryImplV#.init()을 직접 호출하여 cache안에 데이터가 들어가게 함
     */
    @Test
    void getPopularArticles(){
        topViewRepositoryImplV3.init();

        List<Post> result = topViewService.getPopularArticles();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .extracting(Post::getContent, Post::getViewCount)
                .containsExactly(
                        tuple("tsla1", 0L)
                );
    }

}