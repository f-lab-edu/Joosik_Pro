package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Redis와 zSet을 활용한 Bulk Update 구현
 *
 * 최신 인기 글 순위를 정확하게 반영하려면 getPopularPosts 메서드를 호출할 때마다
 * zSet을 이용해 캐시를 업데이트하는 것이 좋지만, 성능 문제가 발생할 수 있음.
 * zSet에 쌓인 조회수를 DB에 반영할 때, 새로운 캐시 데이터를 업데이트.
 *
 * returnPost 호출 시, 캐시에 값이 있는지 확인:
 *  캐시에 존재하면 캐시에서 반환.
 *  캐시에 없으면 DB에서 직접 조회 후 반환.
 *
 * 캐시 데이터를 DB에 업데이트할 때:
 *  캐시에 값이 있으면 zSet에 총 조회수가 저장됨.
 *  캐시에 값이 없으면 zSet의 조회수가 0부터 시작되므로, 기존 조회수에 zSet의 누적 조회수를 더하여 반영.
 *
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTopViewRepositoryImpl implements TopViewRepositoryV2 {

    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;

    private static final LinkedHashMap<Long, Post> cache = new LinkedHashMap<>();
    private static final String POPULAR_POSTS_SET_KEY = "popularPostsZSet";

    @PostConstruct
    private void init() {
        updateCacheWithDB();
    }

    @Override
    public void updateCacheWithDBAutomatically() {
        updateCacheWithDB();
    }

    @Override
    public Post returnPost(Long postId) {
        Post post = cache.get(postId);
        if(post == null){
            post = postRepository.findById(postId);
        }
        redisTemplate.opsForZSet().incrementScore(POPULAR_POSTS_SET_KEY, String.valueOf(postId), 1.0);
        return post;
    }

    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        return cache;
    }

    private void updateCacheWithDB() {
        updateViewCountsToDB();
        redisTemplate.delete(POPULAR_POSTS_SET_KEY);
        cache.clear();

        var topPosts = postRepository.getPopularArticles();
        for (Post post : topPosts) {
            Long postId = post.getId();
            redisTemplate.opsForZSet().add(POPULAR_POSTS_SET_KEY,
                    String.valueOf(post.getId()),
                    (double) post.getViewCount());
            cache.put(postId, post); // 캐시에 추가
        }
    }

    private void updateViewCountsToDB() {
        Set<String> allPostIds = redisTemplate.opsForZSet().range(POPULAR_POSTS_SET_KEY, 0, -1);
        if (allPostIds.isEmpty()) return;
        for (String idStr : allPostIds) {
            try {
                Long postId = Long.parseLong(idStr);
                Double score = redisTemplate.opsForZSet().score(POPULAR_POSTS_SET_KEY, idStr);
                Post post = cache.get(postId);
                if(post!= null){
                    post.setViewCount(score.longValue());
                }else{
                    post = postRepository.findById(postId);
                    post.increaseViewCount(score.longValue());
                }
            } catch (Exception e) {
                log.warn("ZSet 조회수 동기화 실패 - id: {}", idStr, e);
            }
        }
    }
}
