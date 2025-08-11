package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.PostDtoResponse;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

import java.util.*;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class RedisTopViewRepositoryImpl_FinalTest {

    @Autowired private RedisTopViewRepositoryImpl_Final topViewRepository;
    @Autowired private RedisTemplate<String, String> redisTemplate;

    private Post post1, post2, post3, post4, post5, post6, post7, post8, post9, post10, post11;
    @Autowired private EntityManager em;
    @Autowired private PostRepository postRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        // Redis / DB 초기화
        redisTemplate.getConnectionFactory().getConnection().flushAll();

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

        // 초기 동기화
        topViewRepository.updateCacheWithDBAutomatically();
    }

    @AfterEach
    void clearCache() throws Exception {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @DisplayName("캐시 삭제 후 새로 채워졌는지 확인")
    @Test
    void 전체적인_레디스_작동_확인() {
        final String ZSET_KEY = "popularPostsZSet";
        Set<String> keysBefore = redisTemplate.keys("post:*");
        // 확실히 10개가 들어왔는지
        assertThat(keysBefore).hasSize(10);
        // 조회수에 따라서 게시글 1은 안들어오고 게시글 11은 들어왔는지
        assertThat(keysBefore).doesNotContain("post:" + post1.getId()); // 1 제외
        assertThat(keysBefore).contains("post:" + post11.getId()); // 11 포함

        //
        int hits = 15;
        for (int i = 0; i < hits; i++) {
            topViewRepository.returnPost(post1.getId()); // 내부에서 ZSET score +1
        }

        em.flush();
        em.clear();

        Post p1After = postRepository.findById(post1.getId());
        assertThat(p1After.getViewCount()).isEqualTo(16L);


        int hits2 = 5;
        for (int i = 0; i < hits2; i++) {
            topViewRepository.returnPost(post10.getId()); // 내부에서 ZSET score +5
        }

        // ZSET에 조회수가 반영되는지 학인
        Double zscoreBeforePersist = redisTemplate.opsForZSet()
                .score(ZSET_KEY, String.valueOf(post10.getId()));
        assertThat(zscoreBeforePersist).isNotNull();
        assertThat(zscoreBeforePersist.intValue()).isEqualTo(15);

        // 순서도 제대로 바뀌었는지 확인

        // when
        topViewRepository.updateCacheWithDBAutomatically();

        // 업데이트가 제대로 이루어지는지 확인
        Set<String> keysAfter = redisTemplate.keys("post:*");
        assertThat(keysAfter).hasSize(10);
        assertThat(keysAfter).doesNotContain("post:" + post2.getId());    // 2 탈락
        assertThat(keysAfter).contains("post:" + post1.getId());         // 1 진입


        Double zscoreBeforePersist2 = redisTemplate.opsForZSet()
                .score(ZSET_KEY, String.valueOf(post1.getId()));

        assertThat(zscoreBeforePersist2.intValue()).isEqualTo(16);


        // 순서가 제대로 바뀌었는지 확인
        List<String> Ranking = new ArrayList<>(redisTemplate.opsForZSet().reverseRange(ZSET_KEY, 0, 9));
        assertThat(Ranking).containsExactly(
                String.valueOf(post1.getId()),
                String.valueOf(post10.getId()),
                String.valueOf(post11.getId()),
                String.valueOf(post9.getId()),
                String.valueOf(post8.getId()),
                String.valueOf(post7.getId()),
                String.valueOf(post6.getId()),
                String.valueOf(post5.getId()),
                String.valueOf(post4.getId()),
                String.valueOf(post3.getId())
        );

    }

}
