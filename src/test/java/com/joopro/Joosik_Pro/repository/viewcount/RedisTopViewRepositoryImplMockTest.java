package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.PostRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@SpringBootTest
class RedisTopViewRepositoryImplMockTest {

    @Autowired
    private RedisTopViewRepositoryImpl redisTopViewRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private PostRepository postRepository;

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

        Stock stock = Stock.builder()
                .companyName("Apple")
                .sector("Tech")
                .ticker("AAPL")
                .build();

        post = SingleStockPost.makeSingleStockPost("애플 분석", member, stock);
        post.increaseViewCount(100L);

        given(postRepository.getPopularArticles()).willReturn(List.of(post));
        given(postRepository.findById(testPostId)).willReturn(post);

        setPostId(post, 1L);

        // 캐시 초기화 실행
        redisTopViewRepository.updateCacheWithDBAutomatically();
    }

    @AfterEach
    void teardown(){
        redisTemplate.delete("popularPostsZSet");
    }

    @Test
    @DisplayName("게시글 조회 시 ZSet 점수가 증가해야 한다.")
    void returnPost() {
        redisTopViewRepository.returnPost(testPostId);

        Double score = redisTemplate.opsForZSet().score("popularPostsZSet", testPostId.toString());
        assertThat(score).isEqualTo(101L);
    }

    @Test
    @DisplayName("updateViewCountsToDB 호출 시 캐시된 조회수가 DB(Post)에 반영되어야 한다.")
    void updateViewCountsToDB() {
        redisTopViewRepository.returnPost(testPostId); // 조회수 증가 1회

        redisTopViewRepository.updateCacheWithDBAutomatically();

        for(Map.Entry<Long, Post> entry : redisTopViewRepository.getPopularPosts().entrySet()){
            System.out.println("entry.getKey : " + entry.getKey());
            System.out.println("entry.getValue : " + entry.getValue());
        }


        Post updatedPost = redisTopViewRepository.getPopularPosts().get(testPostId);
        assertThat(updatedPost.getViewCount()).isEqualTo(101L); // 기존 100 + 1
    }

    // 실제 객체는 Mockito로 Stubbing 불가, 처음엔 given(post.getId()).willReturn(1L); 썼지만 안됨
    // 리플렉션 코드
    private void setPostId(Post post, Long id) {
        try {
            Field idField = Post.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(post, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
