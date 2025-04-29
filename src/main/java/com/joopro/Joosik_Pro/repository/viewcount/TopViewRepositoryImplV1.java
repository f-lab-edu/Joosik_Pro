
package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.sun.jdi.LongType;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.sql.SQLOutput;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TopViewRepository 역할 수행
 *
 * Top10SingleStockPost에 이미 값을 저장해두고 거기에 값이 있다면 값 반환 없다면 DB로 연결
 * cacheHit 라는 수를 둬서 cache에 조회수 저장, 나중에 100이 넘어가면 한번에 업데이트
 */
@Repository
@RequiredArgsConstructor
public class TopViewRepositoryImplV1 implements TopViewRepository{

    private final PostRepository postRepository;
    private final EntityManager em;
    @Getter
    private static AtomicInteger cacheHit = new AtomicInteger();
    private static LinkedHashMap<Long, Post> Top100Post = new LinkedHashMap<>();
    @Getter
    private static final Map<Long, Integer> tempViewCount = new ConcurrentHashMap<>();


    @PostConstruct
    public void init() {
        updateCacheWithDB();
    }

    @Override
    public void bulkUpdatePostViews(Long postId) {
        tempViewCount.put(postId, tempViewCount.getOrDefault(postId, 0) + 1);

        if (cacheHit.incrementAndGet() >= 100) {
            updateViewCountsToDB();
            cacheHit.set(0);
        }
    }


    // 상위 10개 가져오는 코드
    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        return Top100Post;
    }

    /**
     * ImplV1에서는 조회수가 실시간으로 cache에 반영되지 않기때문에 그냥 updateCacheWithDB로 내부적으로 동작하게 해두겠습니다.
     */
    public void updateCacheInLocal() {
        updateCacheWithDB();
    }

    public void updateCacheWithDB() {
        updateViewCountsToDB();
        List<Post> topPosts = postRepository.getPopularArticles();

        LinkedHashMap<Long, Post> cacheMap = new LinkedHashMap<>();
        for (Post post : topPosts) {
            cacheMap.put(post.getId(), post);
        }
        Top100Post = cacheMap;
    }


    // DB와 sync 맞추는 코드
    public void updateViewCountsToDB() {
        for(Map.Entry<Long, Integer> entry : tempViewCount.entrySet()){
            Long firstValue = postRepository.findById(entry.getKey()).getViewCount();
            Post post = postRepository.findById(entry.getKey());
            post.setViewCount(Long.valueOf(firstValue + entry.getValue()));
            System.out.println(postRepository.findById(entry.getKey()).getViewCount());

            // 이게 왜 안되지?
//            em.createQuery("UPDATE Post p SET p.viewCount = p.viewCount + :increment WHERE p.id = :postId")
//                    .setParameter("increment", entry.getValue())
//                    .setParameter("postId", entry.getKey())
//                    .executeUpdate();
//            System.out.println(entry.getKey());
//            System.out.println(entry.getValue());
//
//            System.out.println(postRepository.findById(entry.getKey()).getViewCount());
        }
        tempViewCount.clear();
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
