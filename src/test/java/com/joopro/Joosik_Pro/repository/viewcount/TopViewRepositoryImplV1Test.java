package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

@SpringBootTest
@Transactional
class TopViewRepositoryImplV1Test {

    @Autowired private EntityManager em;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private TopViewRepositoryImplV1 topViewRepository;

    private Post post1, post2, post3, post4, post5, post6, post7, post8, post9, post10, post11;

    @BeforeEach
    void setUp() {
        // 실제 엔티티 저장
        Member member = Member.builder()
                .name("유저A")
                .password("1234")
                .email("a@email.com")
                .build();
        memberRepository.save(member);

        Stock stock = Stock.builder()
                .companyName("Tesla")
                .sector("CAR")
                .ticker("TSLA")
                .build();
        stockRepository.save(stock);

        post1 = SingleStockPost.makeSingleStockPost("내용1", member, stock);
        post2 = SingleStockPost.makeSingleStockPost("내용2", member, stock);
        post3 = SingleStockPost.makeSingleStockPost("내용3", member, stock);
        post4 = SingleStockPost.makeSingleStockPost("내용4", member, stock);
        post5 = SingleStockPost.makeSingleStockPost("내용5", member, stock);
        post6 = SingleStockPost.makeSingleStockPost("내용6", member, stock);
        post7 = SingleStockPost.makeSingleStockPost("내용7", member, stock);
        post8 = SingleStockPost.makeSingleStockPost("내용8", member, stock);
        post9 = SingleStockPost.makeSingleStockPost("내용9", member, stock);
        post10 = SingleStockPost.makeSingleStockPost("내용10", member, stock);
        post11 = SingleStockPost.makeSingleStockPost("내용11", member, stock);

        post1.increaseViewCount(1L);
        post2.increaseViewCount(2L);
        post3.increaseViewCount(3L);
        post4.increaseViewCount(4L);
        post5.increaseViewCount(5L);
        post6.increaseViewCount(6L);
        post7.increaseViewCount(7L);
        post8.increaseViewCount(8L);
        post9.increaseViewCount(9L);
        post10.increaseViewCount(10L);
        post11.increaseViewCount(11L);

        postRepository.save(post1);
        postRepository.save(post2);
        postRepository.save(post3);
        postRepository.save(post4);
        postRepository.save(post5);
        postRepository.save(post6);
        postRepository.save(post7);
        postRepository.save(post8);
        postRepository.save(post9);
        postRepository.save(post10);
        postRepository.save(post11);

        em.flush();
        em.clear();

        topViewRepository.updateCacheWithDB();
    }

    @AfterEach
    void reset(){
        TopViewRepositoryImplV1.getTempViewCount().clear();
        TopViewRepositoryImplV1.getCacheHit().set(0);
    }

    @Test
    @DisplayName("Top100Post는 초기화 시 상위 게시글들을 잘 포함하고 있다.")
    void getPopularPosts() {
        Map<Long, Post> result = topViewRepository.getPopularPosts();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result.values())
                .extracting("content", "viewCount")
                .containsExactly(
                        tuple("내용11", 11L),
                        tuple("내용10", 10L),
                        tuple("내용9", 9L),
                        tuple("내용8", 8L),
                        tuple("내용7", 7L),
                        tuple("내용6", 6L),
                        tuple("내용5", 5L),
                        tuple("내용4", 4L),
                        tuple("내용3", 3L),
                        tuple("내용2", 2L)
                );
    }

    @Test
    @DisplayName("Top100Post에 있는 게시글은 tempViewCount로 조회수 증가")
    void bulkUpdatePostViews() {
        topViewRepository.bulkUpdatePostViews(post4.getId());
        topViewRepository.bulkUpdatePostViews(post4.getId());

        // Top100에 있으므로 tempViewCount에서 증가해야 함
        assertThat(TopViewRepositoryImplV1.getTempViewCount().get(post4.getId())).isEqualTo(2);
        assertThat(TopViewRepositoryImplV1.getCacheHit().get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Top100Post에 있는 게시글은 tempViewCount로 조회수 증가")
    void bulkUpdatePostViewsOver100() {
        // 조회수 증가 120회
        for (int i = 0; i < 120; i++) {
            topViewRepository.bulkUpdatePostViews(post4.getId());
        }

        // tempViewCount가 20이 되어야 함
        Integer tempCount = TopViewRepositoryImplV1.getTempViewCount().get(post4.getId());

        assertThat(tempCount).isEqualTo(20);
        assertThat(postRepository.findById(post4.getId()).getViewCount()).isEqualTo(104L);

        // cacheHit도 20이 되어야 함
        assertThat(TopViewRepositoryImplV1.getCacheHit().get()).isEqualTo(20);

    }

    @Test
    @DisplayName("tempViewCount를 DB에 반영하고 tempViewCount, cacheHit 초기화")
    void updateCacheWithDB() {
        for (int i = 0; i < 120; i++) {
            topViewRepository.bulkUpdatePostViews(post4.getId());
        }
        Post result = postRepository.findById(post4.getId());
        assertThat(result.getViewCount()).isEqualTo(104L);
        topViewRepository.updateCacheWithDB();

        Map<Long, Post> resultSet = topViewRepository.getPopularPosts();
        assertThat(resultSet.size()).isEqualTo(10);
        assertThat(resultSet.values())
                .extracting("content", "viewCount")
                .containsExactly(
                        tuple("내용4", 124L),
                        tuple("내용11", 11L),
                        tuple("내용10", 10L),
                        tuple("내용9", 9L),
                        tuple("내용8", 8L),
                        tuple("내용7", 7L),
                        tuple("내용6", 6L),
                        tuple("내용5", 5L),
                        tuple("내용3", 3L),
                        tuple("내용2", 2L)
                );


        assertThat(TopViewRepositoryImplV1.getTempViewCount()).isEmpty();
        assertThat(TopViewRepositoryImplV1.getCacheHit().get()).isEqualTo(20);

    }


    /**
     * tempViewcount를 AtomicInteger이 아닌 그냥 Integer로 했을 때 동시성 이슈 발생
     * @throws InterruptedException
     */
    @Test
    void sameViewUpgradeAtSameTime() throws InterruptedException {
        int threadCount = 150; // 동시에 150번 요청
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        Long testPostId = post1.getId();

        for (int i = 0; i < threadCount; i++) {
            try{
                executorService.submit(() -> {
                    topViewRepository.bulkUpdatePostViews(testPostId);
                });
            }finally{
                latch.countDown();
            }
        }

        latch.await();

        int cachedViewCount = TopViewRepositoryImplV1.getTempViewCount().get(testPostId);
        assertThat(cachedViewCount).isEqualTo(threadCount-100);
    }



}