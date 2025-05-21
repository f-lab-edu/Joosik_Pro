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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
class RedisTopViewRepositoryImplTest {

    @Autowired
    private RedisTopViewRepositoryImpl topViewRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

//    @Mock
    @Autowired private EntityManager em;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StockRepository stockRepository;

    private final Long testPostId = 1L;

    private Post post;

    @BeforeEach
    void setUp() {
        redisTemplate.delete("popularPostsZSet");

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

        post = SingleStockPost.makeSingleStockPost("애플 분석", member, stock);
        postRepository.save(post);

        post.increaseViewCount(100L);

        em.flush();
        em.clear();

        // 캐시 초기화 실행
        topViewRepository.updateCacheWithDBAutomatically();
    }

    @AfterEach
    void teardown(){
        redisTemplate.delete("popularPostsZSet");
    }

    @Test
    @DisplayName("게시글 조회 시 ZSet 점수가 증가해야 한다.")
    void returnPost() {
        topViewRepository.returnPost(testPostId);

        Double score = redisTemplate.opsForZSet().score("popularPostsZSet", testPostId.toString());
        assertThat(score).isEqualTo(1L);
    }

    @Test
    @DisplayName("updateViewCountsToDB 호출 시 캐시된 조회수가 DB(Post)에 반영되어야 한다.")
    void updateViewCountsToDB() {
        topViewRepository.returnPost(testPostId); // 조회수 증가 1회


        topViewRepository.updateCacheWithDBAutomatically();

        for(Map.Entry<Long, Post> entry : topViewRepository.getPopularPosts().entrySet()){
            System.out.println("entry.getKey : " + entry.getKey());
            System.out.println("entry.getValue : " + entry.getValue());
        }


        Post updatedPost = topViewRepository.getPopularPosts().get(testPostId);
        assertThat(updatedPost.getViewCount()).isEqualTo(101L); // 기존 100 + 1
    }

}
