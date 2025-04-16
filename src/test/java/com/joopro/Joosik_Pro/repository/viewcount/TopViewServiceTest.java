package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import com.joopro.Joosik_Pro.service.MemberService;
import com.joopro.Joosik_Pro.service.PostService;
import com.joopro.Joosik_Pro.service.StockService;
import com.joopro.Joosik_Pro.service.TopViewService.TopViewService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;


/**
 * 동시성 테스트는 특정 로직구간을 검증하기 위한 것이 아닌 사용자가 많이 몰리는 경우를 가정
 * 따라서 Service 레이어에서 테스트해야함
 *
 */
//@TestPropertySource(locations = "classpath:application-test.properties")
@SpringBootTest
public class TopViewServiceTest {

    @Autowired TopViewService topViewService;
    @Autowired private PostService postService;
    @Autowired private MemberService memberService;
    @Autowired private StockService stockService;

    private Post post1, post2, post3, post4, post5, post6, post7, post8, post9, post10, post11;

    @BeforeEach
    void setUp() {

        // 실제 엔티티 저장
        Member member = Member.builder()
                .name("유저A")
                .password("1234")
                .email("a@email.com")
                .build();
        memberService.save(member);

        Stock stock = Stock.builder()
                .companyName("Tesla")
                .sector("CAR")
                .ticker("TSLA")
                .build();
        stockService.saveStock(stock);

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

    }

    @Test
    void synchronizeTest() {
        int totalThreads = 200;
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(totalThreads);

        for (int i = 0; i < totalThreads; i++) {
            executorService.submit(() -> {
                try {
                    topViewService.returnPost(1L);
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
    }

    @Test
    void getPopularArticles(){
        List<Post> result = topViewService.getPopularArticles();
        assertThat(result.size()).isEqualTo(10);
        assertThat(result)
                .extracting(Post::getContent, Post::getViewCount)
                .containsExactly(
                        tuple("내용11", 11L),
                        tuple("내용10", 10L),
                        tuple("내용9", 9L),
                        tuple("내용8", 8L),
                        tuple("내용7", 7L),
                        tuple("내용6", 6L),
                        tuple("내용5", 5L),
                        tuple("내용2", 5L),
                        tuple("내용4", 4L),
                        tuple("내용1", 4L)
                );
    }

}