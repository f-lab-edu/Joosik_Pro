package com.joopro.Joosik_Pro.repository.viewcount;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RedisTopViewRepositoryImpl_Final implements TopViewRepositoryV2{
    private final PostRepository postRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String POPULAR_POSTS_SET_KEY = "popularPostsZSet";

    @Transactional
    @PostConstruct
    protected void init() {
        syncDbToRedis();
    }

    @Transactional
    @Override
    public void updateCacheWithDBAutomatically() {
        updateViewCountsToDB();
        syncDbToRedis();
    }

    @Override
    public Post returnPost(Long postId) {
        String redisKey = "post:" + postId;
        String postJson = redisTemplate.opsForValue().get(redisKey);
        Post post = null;

        if (postJson != null) {
            try {
                post = objectMapper.readValue(postJson, Post.class);
            } catch (Exception e) {
                log.warn("Redis에서 Post JSON 역직렬화 실패, DB 조회로 대체", e);
            }
        }
        // 캐시에 없으면 DB에서 조회 (레디스에는 넣지 않음!)
        if (post == null) {
            post = postRepository.findById(postId);
        }

        // 조회수 증가 (ZSet에서만 관리)
        redisTemplate.opsForZSet().incrementScore(POPULAR_POSTS_SET_KEY, String.valueOf(postId), 1.0);
        return post;
    }

    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        Set<String> postIdStrSet = redisTemplate.opsForZSet()
                .reverseRange(POPULAR_POSTS_SET_KEY, 0, 9);

        if (postIdStrSet == null || postIdStrSet.isEmpty()) {
            return new LinkedHashMap<>();
        }

        List<Long> postIds = postIdStrSet.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());

        // Redis에서 Post 정보 한 번에 꺼내오기
        LinkedHashMap<Long, Post> result = new LinkedHashMap<>();
        for (Long postId : postIds) {
            String redisKey = "post:" + postId;
            String postJson = redisTemplate.opsForValue().get(redisKey);
            Post post = null;
            if (postJson != null) {
                try {
                    post = objectMapper.readValue(postJson, Post.class);
                } catch (Exception e) {
                    log.warn("Redis에서 Post JSON 역직렬화 실패, DB 조회로 대체", e);
                }
            }
            // 없으면 DB에서 조회
            if (post == null) {
                post = postRepository.findById(postId);
            }
            if (post != null) {
                result.put(postId, post);
            }
        }
        return result;
    }

    private void updateViewCountsToDB() {
        Set<String> allPostIds = redisTemplate.opsForZSet().range(POPULAR_POSTS_SET_KEY, 0, -1);
        if (allPostIds == null || allPostIds.isEmpty()) return;
        for (String idStr : allPostIds) {
            try {
                Long postId = Long.parseLong(idStr);
                Double score = redisTemplate.opsForZSet().score(POPULAR_POSTS_SET_KEY, idStr);
                if (score == null) continue;
                Post post = postRepository.findById(postId);
                if (post != null) {
                    post.setViewCount(score.longValue());
                    postRepository.save(post);
                }
            } catch (Exception e) {
                log.warn("ZSet 조회수 동기화 실패 - id: {}", idStr, e);
            }
        }
    }

    private void syncDbToRedis() {
        redisTemplate.delete(POPULAR_POSTS_SET_KEY);
        List<Post> topPosts = postRepository.getPopularArticles();
        for (Post post : topPosts) {
            Long postId = post.getId();
            String redisKey = "post:" + postId;
            // ZSet에 순위 저장
            redisTemplate.opsForZSet().add(POPULAR_POSTS_SET_KEY,
                    String.valueOf(postId),
                    (double) post.getViewCount());
            // Key-Value로 게시글 본문도 같이 캐싱
            try {
                String toJson = objectMapper.writeValueAsString(post);
                redisTemplate.opsForValue().set(redisKey, toJson);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
