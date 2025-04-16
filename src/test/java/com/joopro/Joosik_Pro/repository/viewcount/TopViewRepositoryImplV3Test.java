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

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

/**
 * TopViewRepositoryImplV3 단위 테스트
 */
@SpringBootTest
@Transactional
class TopViewRepositoryImplV3Test {

    @Autowired
    private EntityManager em;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private TopViewRepositoryImplV3 topViewRepositoryImplV3;

    private Post post1, post2;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .name("유저B")
                .password("1234")
                .email("b@email.com")
                .build();
        memberRepository.save(member);

        Stock stock = Stock.builder()
                .companyName("Apple")
                .sector("Tech")
                .ticker("AAPL")
                .build();
        stockRepository.save(stock);

        post1 = SingleStockPost.makeSingleStockPost("애플 분석", member, stock);
        post2 = SingleStockPost.makeSingleStockPost("아이폰 전망", member, stock);
        post1.increaseViewCount(5L);
        post2.increaseViewCount(3L);

        postRepository.save(post1);
        postRepository.save(post2);

        em.flush();
        em.clear();

        topViewRepositoryImplV3.init(); // PostConstruct 수동 호출
    }

    @AfterEach
    void clearCache() {
        topViewRepositoryImplV3.getCache().clear();
        TopViewRepositoryImplV3.getTempViewCount().clear();
    }

    @Test
    @DisplayName("조회수를 올리면 tempViewCount에 값이 누적된다.")
    void updateTempViewCountTest() {
        topViewRepositoryImplV3.returnPost(post1.getId());
        topViewRepositoryImplV3.returnPost(post1.getId());

        AtomicInteger count = TopViewRepositoryImplV3.getTempViewCount().get(post1.getId());
        assertThat(count).isNotNull();
        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateViewCountsToDB를 호출하면 DB 조회수에 반영되고 tempViewCount는 초기화된다.")
    void updateViewCountsToDBTest() {
        topViewRepositoryImplV3.returnPost(post2.getId());
        topViewRepositoryImplV3.returnPost(post2.getId());

        topViewRepositoryImplV3.init(); // updateViewCountsToDB 포함

        Post updatedPost = postRepository.findById(post2.getId());
        assertThat(updatedPost.getViewCount()).isEqualTo(5L); // 기존 3L + 2

        assertThat(TopViewRepositoryImplV3.getTempViewCount()).isEmpty();
    }

    @Test
    @DisplayName("캐시에 인기 게시글 TOP10이 유지된다.")
    void getPopularPostsTest() {
        LinkedHashMap<Long, Post> popularPosts = topViewRepositoryImplV3.getPopularPosts();
        assertThat(popularPosts).isNotNull();
        assertThat(popularPosts.size()).isLessThanOrEqualTo(10);
        assertThat(popularPosts.values())
                .extracting("content", "viewCount")
                .contains(tuple("애플 분석", 5L), tuple("아이폰 전망", 3L));
    }
}

