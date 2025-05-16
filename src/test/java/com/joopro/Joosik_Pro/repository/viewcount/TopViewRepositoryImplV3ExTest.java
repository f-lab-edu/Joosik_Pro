package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.when;

/**
 * TopViewRepositoryImplV3 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
class TopViewRepositoryImplV3ExTest {

    @InjectMocks
    private TopViewRepositoryImplV3 topViewRepositoryImplV3;

    @Mock
    private PostRepository postRepository;

    private Post post1, post2;

    @BeforeEach
    void setUp() {
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

        post1 = SingleStockPost.makeSingleStockPost("애플 분석", member, stock);
        post2 = SingleStockPost.makeSingleStockPost("애플 전망", member, stock);

        post1.increaseViewCount(5L);
        post2.increaseViewCount(3L);
        post1.setId(1L);
        post2.setId(2L);

        when(postRepository.getPopularArticles()).thenReturn(List.of(post1, post2));

        topViewRepositoryImplV3.updateCacheWithDBAutomatically();
    }

    @AfterEach
    void clearCache() throws Exception {
        LinkedHashMap<Long, Post> cache = accessCacheByReflection();
        Map<Long, AtomicInteger> tempViewMap = accessTempViewCountByReflection();
        cache.clear();
        tempViewMap.clear();
    }

    @Test
    @DisplayName("조회수를 올리면 tempViewCount에 값이 누적된다.")
    void updateTempViewCountTest() throws Exception {
        topViewRepositoryImplV3.returnPost(post1.getId());
        topViewRepositoryImplV3.returnPost(post1.getId());

        Map<Long, AtomicInteger> tempViewMap = accessTempViewCountByReflection();

        AtomicInteger count = tempViewMap.get(post1.getId());
        assertThat(count).isNotNull();
        assertThat(count.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("updateViewCountsToDB를 호출하면 DB 조회수에 반영되고 tempViewCount는 초기화된다.")
    void updateViewCountsToDBTest() throws Exception {
        topViewRepositoryImplV3.returnPost(post2.getId());
        topViewRepositoryImplV3.returnPost(post2.getId());

        // Mock 설정
        when(postRepository.findById(post2.getId())).thenReturn(post2);

        topViewRepositoryImplV3.updateCacheWithDBAutomatically();
        Map<Long, AtomicInteger> tempViewMap = accessTempViewCountByReflection();
        assertThat(tempViewMap).isEmpty();
        assertThat(post2.getViewCount()).isEqualTo(5L); // 기존 3 + 2
    }

    @Test
    @DisplayName("캐시에 인기 게시글 TOP10이 유지된다.")
    void getPopularPostsTest() {
        LinkedHashMap<Long, Post> popularPosts = topViewRepositoryImplV3.getPopularPosts();
        assertThat(popularPosts).isNotNull();
        assertThat(popularPosts.size()).isLessThanOrEqualTo(10);
        assertThat(popularPosts.values())
                .extracting("content", "viewCount")
                .contains(tuple("애플 분석", 5L), tuple("애플 전망", 3L));
    }

    @SuppressWarnings("unchecked")
    private Map<Long, AtomicInteger> accessTempViewCountByReflection() throws Exception {
        var method = TopViewRepositoryImplV3.class.getDeclaredMethod("getTempViewCountForTest");
        method.setAccessible(true);
        return (Map<Long, AtomicInteger>) method.invoke(topViewRepositoryImplV3);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<Long, Post> accessCacheByReflection() throws Exception {
        var method = TopViewRepositoryImplV3.class.getDeclaredMethod("getCacheForTest");
        method.setAccessible(true);
        return (LinkedHashMap<Long, Post>) method.invoke(topViewRepositoryImplV3);
    }
}


