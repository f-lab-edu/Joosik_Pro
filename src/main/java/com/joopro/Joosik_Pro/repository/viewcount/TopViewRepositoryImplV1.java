
package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.repository.PostRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
    private static AtomicInteger cacheHit = new AtomicInteger();
    private static LinkedHashMap<Long, Post> Top100Post = new LinkedHashMap<>();
    private static final Map<Long, Integer> tempViewCount = new ConcurrentHashMap<>();


    @Override
    public void bulkUpdatePostViews(Long postId) {
        updateViewCountsToDB(postId);
    }


    // 상위 10개 가져오는 코드
    @Override
    public LinkedHashMap<Long, Post> getPopularPosts() {
        return Top100Post;
    }

    public LinkedHashMap<Long, Post> updateCache() {
        List<Post> topPosts = em.createQuery("SELECT p FROM Post p ORDER BY p.viewCount DESC", Post.class)
                .setMaxResults(100)
                .getResultList();

        LinkedHashMap<Long, Post> cacheMap = new LinkedHashMap<>();
        for (Post post : topPosts) {
            cacheMap.put(post.getId(), post);
        }
        Top100Post = cacheMap;
        return Top100Post;
    }

    // tempviewCount가 100개 들어올때까지 저장, tempViewCount가 100개 넘었을 때 updateViewCountsToDB 호출, DB와 sync 맞추기
    public void updateViewCountsToDB(Long postId) {

        tempViewCount.put(postId, tempViewCount.getOrDefault(postId, 0) + 1);

        if (cacheHit.incrementAndGet() >= 100) {
            syncViewCountsToDB();
            cacheHit.set(0);
        }
    }

    // DB와 sync 맞추는 코드
    public void syncViewCountsToDB() {
        for(Map.Entry<Long, Integer> entry : tempViewCount.entrySet()){
            em.createQuery("UPDATE Post p SET p.viewCount = p.viewCount + :increment WHERE p.id = :postId")
                    .setParameter("increment", entry.getValue())
                    .setParameter("postId", entry.getKey())
                    .executeUpdate();
        }
        tempViewCount.clear();
    }


}
