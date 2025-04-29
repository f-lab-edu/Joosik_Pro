
package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * cache에 값이 있다면 increaseViewcount로 조회수 올리기 없다면 직접 찾아서 조회수 올리기
 * Service에서 1시간 후에 데이터베이스에 조회수 연동
 * 30분에 한번씩 sorCacheByViewCount로 조회수 기준 내림차순 정렬
 *
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class TopViewRepositoryImplV2 implements TopViewRepository{

    private final EntityManager em;
    private final PostRepository postRepository;

    @Getter //테스트용 Getter
    private LinkedHashMap<Long, AtomicInteger> cache = new LinkedHashMap<>();
    @Getter // 테스트용 Getter
    private LinkedHashMap<Long, Post> returnCache = new LinkedHashMap<>();

    @Transactional
    @PostConstruct
    public void init() {
        updateCacheWithDB();
    }

    // viewCount cache에 있는지 확인하고 있다면 cache에서 증가시키는 로직
    @Override
    public void bulkUpdatePostViews(Long postId) {
        AtomicInteger currentCount = cache.get(postId);
        if (currentCount != null) {
            currentCount.incrementAndGet(); // 직접 증가시키기
        }else{
            Post post = em.find(Post.class, postId);
            post.increaseViewCount(1L);
        }
    }

    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        return returnCache;
    }

    public void updateCacheInLocal() {
        cache = cache.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().longValue(), e1.getValue().longValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
        returnCache = cache.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> postRepository.findById(entry.getKey()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    public void updateCacheWithDB() {
        updateViewCountsToDB();
        cache = postRepository.getPopularArticles().stream()
                .collect(Collectors.toMap(
                        Post::getId,
                        post -> new AtomicInteger(post.getViewCount().intValue()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));

        returnCache = cache.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> postRepository.findById(entry.getKey()),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    // 데이터베이스에 조회수 연동
    public void updateViewCountsToDB(){
        log.info("updateViewCountsToDB start");
        for(Map.Entry<Long, AtomicInteger> entry : cache.entrySet()) {
            log.info("entry.getKey() : {}", entry.getKey());
            log.info("entry.getValue() : {}", entry.getValue());

            log.info("여기다");
            List<Post> posts = postRepository.findAll();
            for (Post go : posts) {
                System.out.println("여기는 되냐");
                System.out.println("posts: " + go.getId());
                System.out.println("posts: " + go.getViewCount());
            }
            Post post = em.find(Post.class, entry.getKey());

            if (post != null) {
                log.info("updateViewCountsToDB: post.setViewCount: {}", entry.getValue().longValue());
                post.setViewCount(entry.getValue().longValue());
            }else{
                log.info("null입니다.");
            }
        }
        cache.clear();
    }


    @Scheduled(fixedRate = 600000)
    private void updateCacheWithDBAutomatically() {
        this.updateCacheWithDB();
    }

    @Scheduled(fixedRate = 30000)
    private void updateCacheInLocalAutomatically(){
        this.updateCacheInLocal();
    }



}
