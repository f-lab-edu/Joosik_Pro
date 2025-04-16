package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Primary
@Repository @RequiredArgsConstructor
public class TopViewRepositoryImplV3 implements TopViewRepositoryV2{

    private final PostRepository postRepository;

    // 테스트용 Getter
    @Getter private LinkedHashMap<Long, Post> cache = new LinkedHashMap<>();
    @Getter private static final Map<Long, AtomicInteger> tempViewCount = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        updateCacheWithDB();
    }

    @Override
    public Post returnPost(Long postId) {
        Post post = cache.get(postId);
        if(post!= null){
            tempViewCount.computeIfAbsent(postId, id -> new AtomicInteger(0)).incrementAndGet();
//            tempViewCount.put(postId, tempViewCount.getOrDefault(postId, 0) + 1);
        }else{
            post = postRepository.findById(postId);
            post.increaseViewCount(1L);
        }
        return post;
    }

    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        return cache;
    }

    private void updateCacheWithDB() {
        updateViewCountsToDB();
        cache = postRepository.getPopularArticles().stream()
                .collect(Collectors.toMap(
                        Post::getId,
                        post -> post,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    // 데이터베이스에 조회수 연동
    private void updateViewCountsToDB(){
        for(Map.Entry<Long, AtomicInteger> entry : tempViewCount.entrySet()) {
            Post post = postRepository.findById(entry.getKey());
            if(post!= null){
                Long tempViewCount = entry.getValue().longValue() + post.getViewCount();
                post.setViewCount(tempViewCount);
            }else{
                log.info("null입니다.");
            }
        }
        tempViewCount.clear();
    }

    @Scheduled(cron = "${scheduler.topview.cron}")
    private void updateCacheWithDBAutomatically() {
        log.info("스케줄러 동작");
        this.updateCacheWithDB();
    }

}
