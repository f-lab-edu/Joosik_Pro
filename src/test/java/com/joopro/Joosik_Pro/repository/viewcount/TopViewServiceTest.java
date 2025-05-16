package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.service.TopViewService.TopViewService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
 * TopViewService에 updateCacheWithDBAutomatically메서드에 Synchornized를 넣지 않았을 때
 * current == 100 즉, 업데이트가 한번만 이뤄질때는 동시성 문제가 생기지 않지만
 * current % 100 == 0 즉, 업데이트(updateCacheWithDBAUtomatically)가 여러 번 이루어지면
 * 동시성 문제가 생긴다
 *
 * DB 반영하고 있는 로직들이 겹쳐서 실행될 수 있다. 따라서 updateCacheWithDBAUtomatically 메서드에 Synchronized를 넣었다.
 *
 * Synchornized를 넣는다고 하더라도 totalThread를 598로 해버리면 500이 DB에업데이트되고 98은 캐시에 업데이트 되야 하는데 598이 모두 업데이트 되는 문제가 발생했다.
 * current == 100이라고 해도 업데이트가 350정도까지 올라가 있는걸 확인했다.
 *
 * 캐시 업데이트를 담당하는 스레드의 속도가 매우 빠르다 보니, updateCacheWithDBAutomatically가 실행되기 전에 이미 많은 스레드들이 완료되었다.
 * incrementAndGet()이 증가하는 속도와 updateCacheWithDBAutomatically의 실행 순서가 맞지 않음을 확인하엿따.
 *
 *
 * 동시성 문제를 해결하려면 인스턴스 단위의 락을 적용하거나 Synchronized 사용을 고민해볼 필요가 있다.
 * 현재 구조에서는 캐시 업데이트 스레드의 속도가 매우 빨라서, DB 반영 전에 스레드가 먼저 완료될 가능성이 크다.
 * 그래도 DB 정합성 측면에서는 데이터를 온전히 반영하고 있기 때문에 완벽한 주기를 맞추지는 못하지만 DB에 정확히 반영되는 것을 확인하였다.
 *
 * 업데이트 스레드의 속도가 매우 빠를 때 캐시에 남은 조회수를 확인하는 테스트 작성
 * DB에 완전히 데이터가 반영되지 않은 경우라도 조회수가 누락되지 않고 캐시에 남은 조회수가 유지되는지 확인하는 테스트 코드를 작성
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

        topViewSchedulerService.updateCacheWithDBAutomatically();

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewService.returnPost(1L);
                    int current = counter.incrementAndGet();
                    if (current % 100 == 0) {
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

        topViewSchedulerService.updateCacheWithDBAutomatically();

        assertEquals(1, topViewService.getPopularArticles().size());
//        assertEquals(500, TopViewRepositoryImplV3.getTempViewCount().get(1L));
        assertEquals(500, postRepository.findById(1L).getViewCount());

    }

    @Test
    void synchronizeTest2() throws Exception {
        int totalThreads = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger counter = new AtomicInteger();

        topViewSchedulerService.updateCacheWithDBAutomatically();

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewService.returnPost(1L);
                    int current = counter.incrementAndGet();
                    if (current == 100) {
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
        Map<Long, AtomicInteger> tempViewMap = accessTempViewCountByReflection();

        assertEquals(1, topViewService.getPopularArticles().size());
        Long remainCacheValue = tempViewMap.get(1L).longValue();
        Long dbValue = postRepository.findById(1L).getViewCount();
        assertEquals(totalThreads, remainCacheValue + dbValue);
    }

    /**
     * 처음에 topViewRepositoryImplV3.init()을 하지 않고 @PostConstruct 안에 있는 init을 사용하면 된다고 생각했지만
     * postConstruct는 처음에 스프링부트(빈)이 뜰 때 작동하므로 @SQL이 적용되기 이전 시점에 작동함 따라서 cache에는 아무꺼도 들어가지 않음
     *
     * 그러므로 테스트를 띄우고 난 뒤 @SQL이 작동하고 데이터가 DB안에 들어간 뒤 topViewRepositoryImplV#.init()을 직접 호출하여 cache안에 데이터가 들어가게 함
     */
    @Test
    void getPopularArticles(){
        topViewRepositoryImplV3.updateCacheWithDBAutomatically();

        List<Post> result = topViewService.getPopularArticles();
        assertThat(result.size()).isEqualTo(1);
        assertThat(result)
                .extracting(Post::getContent, Post::getViewCount)
                .containsExactly(
                        tuple("tsla1", 0L)
                );
    }

    @SuppressWarnings("unchecked")
    private Map<Long, AtomicInteger> accessTempViewCountByReflection() throws Exception {
        var method = TopViewRepositoryImplV3.class.getDeclaredMethod("getTempViewCountForTest");
        method.setAccessible(true);
        return (Map<Long, AtomicInteger>) method.invoke(topViewRepositoryImplV3);
    }

}