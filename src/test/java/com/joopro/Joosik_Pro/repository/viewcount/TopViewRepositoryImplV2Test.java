package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class TopViewRepositoryImplV2Test {

    private static final Logger log = LoggerFactory.getLogger(TopViewRepositoryImplV2Test.class);
    @Autowired private EntityManager em;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private TopViewRepositoryImplV2 topViewRepository;

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
        topViewRepository.getCache().clear();
        topViewRepository.getReturnCache().clear();
    }

    @DisplayName("조회수를 업데이트 할 때 캐시 내에 있다면 캐시 내의 조회수가 증가하고 캐시 내에 없다면 Post에서 직접 증가한다.")
    @Test
    void bulkUpdatePostViews() {
        topViewRepository.bulkUpdatePostViews(post1.getId());
        topViewRepository.bulkUpdatePostViews(post11.getId());

        assertThat(topViewRepository.getCache().get(post11.getId()).get()).isEqualTo(12);
        assertThat(postRepository.findById(post1.getId()).getViewCount()).isEqualTo(2L);
    }

    @DisplayName("PostRepository에 저장되어 있는 게시글 중 조회수 TOP10개를 캐시에 저장한다.")
    @Test
    void getPopularPosts() {
        LinkedHashMap<Long, Post> result = topViewRepository.getPopularPosts();
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

    @DisplayName("로컬캐시의 조회수에 따라서 캐시 내의 순서를 바꾼다. 단 바뀐 조회수가 returnCache(DB)에 반영되진 않는다.")
    @Test
    void updateCacheInLocal() {
        topViewRepository.bulkUpdatePostViews(post2.getId());
        topViewRepository.bulkUpdatePostViews(post2.getId());
        topViewRepository.bulkUpdatePostViews(post2.getId());

        topViewRepository.updateCacheInLocal();

        LinkedHashMap<Long, Post> result = topViewRepository.getPopularPosts();
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
                        tuple("내용2", 2L),
                        tuple("내용4", 4L),
                        tuple("내용3", 3L)
                );
    }

    @DisplayName("DB에서 직접 올린 게시글 조회수까지 포함해서 DB에서 TOP10개를 다시 가져온다")
    @Test
    void updateCacheWithDB() {
        topViewRepository.bulkUpdatePostViews(post2.getId());
        topViewRepository.bulkUpdatePostViews(post2.getId());
        topViewRepository.bulkUpdatePostViews(post2.getId());
        topViewRepository.bulkUpdatePostViews(post1.getId());
        topViewRepository.bulkUpdatePostViews(post1.getId());
        topViewRepository.bulkUpdatePostViews(post1.getId());
        topViewRepository.updateCacheWithDB();
        LinkedHashMap<Long, Post> result = topViewRepository.getPopularPosts();
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
                        tuple("내용2", 5L),
                        tuple("내용5", 5L),
                        tuple("내용1", 4L),
                        tuple("내용4", 4L)
                );


    }

    @DisplayName("캐시 내에 바뀐 조회수를 DB에 업데이트 시킨다.")
    @Test
    void updateViewCountsToDB() {
        topViewRepository.bulkUpdatePostViews(post11.getId());
        topViewRepository.bulkUpdatePostViews(post11.getId());
        topViewRepository.bulkUpdatePostViews(post11.getId());
        topViewRepository.bulkUpdatePostViews(post11.getId());

        topViewRepository.updateViewCountsToDB();
        assertThat(postRepository.findById(post11.getId()).getViewCount()).isEqualTo(15L);

    }

    @Rollback(false)
    @Test
    void synchronizeTest() {
//        long initialViewCount = post11.getViewCount(); // 11
//        Post post = em.find(Post.class, 11L);
//        System.out.println("log1 " + post.getViewCount());
        int totalThreads = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(totalThreads);
        AtomicInteger counter = new AtomicInteger(0);

        System.out.println("postRepository.findAll()");
        List<Post> posts = postRepository.findAll();
        for (Post go : posts) {
            System.out.println("posts: " + go.getId());
            System.out.println("posts: " + go.getViewCount());
        }

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewRepository.bulkUpdatePostViews(post11.getId());
//                    log.info("current counter : " + counter.get());
                    if (counter.incrementAndGet() == 50) {
//                        System.out.println("log4");
                        topViewRepository.updateCacheWithDB();
                        System.out.println("executorService: postRepository.findAll()");
                        List<Post> eposts = postRepository.findAll();
                        for (Post go : eposts) {
                            System.out.println("posts: " + go.getId());
                        }
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

        Post post2 = em.find(Post.class, 11L);
        System.out.println("log5 " + post2.getViewCount());
        System.out.println("log6");
        topViewRepository.updateViewCountsToDB();

        Long finalViewCount = postRepository.findById(post11.getId()).getViewCount();
        System.out.println("finalViewCount : " + finalViewCount);
//        assertThat(finalViewCount).isEqualTo(initialViewCount + totalThreads);

        System.out.println("postRepository.findAll()");
        posts = postRepository.findAll();
        for (Post go : posts) {
            System.out.println("posts임: " + go.getId());
        }
        
    }


}