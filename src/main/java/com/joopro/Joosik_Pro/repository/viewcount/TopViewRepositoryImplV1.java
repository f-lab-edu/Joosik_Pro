package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.SingleStockPost;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Top10SingleStockPost에 이미 값을 저장해두고 거기에 값이 있다면 값 반환 없다면 DB로 연결
 * cacheHit 라는 수를 둬서 cache에 조회수 저장, 나중에 100이 넘어가면 한번에 업데이트
 */
@Repository
@RequiredArgsConstructor
public class TopViewRepositoryImplV1 implements TopViewRepository{

    private final EntityManager em;
    private static final int DB_UPDATE = 100;
    private static int cacheHit = 0;
    private static List<SingleStockPost> Top10SingleStockPost = new ArrayList<>();
    private static final Map<Long, Integer> tempViewCount = new ConcurrentHashMap<>();

    // Top10 로직 구현
    public List<SingleStockPost> getDailyTop10Article() {
        if(Top10SingleStockPost.size() != 10){
            Top10SingleStockPost = getPopularArticles();
        }
        return Top10SingleStockPost;
    }

    // tempviewCount가 100개 들어올때까지 저장, tempViewCount가 100개 넘었을 때 updateViewCountsToDB 호출, DB와 sync 맞추기
    @Override
    public void increaseViewCount(Long postId) {
        tempViewCount.put(postId, tempViewCount.getOrDefault(postId,0) + 1);
        cacheHit++;

        if(cacheHit >= DB_UPDATE){
            updateViewCountsToDB();
            cacheHit = 0;
        }
    }

    // DB와 sync 맞추는 코드
    @Override
    public void updateViewCountsToDB() {
        for(Map.Entry<Long, Integer> entry : tempViewCount.entrySet()){
            em.createQuery("UPDATE SingleStockPost s SET s.article.viewCount = s.article.viewCount + :increment WHERE s.id = :postId")
                    .setParameter("increment", entry.getValue())
                    .setParameter("postId", entry.getKey())
                    .executeUpdate();
        }
        tempViewCount.clear();
    }

    // 1시간 지났을 떄 Top10 refresh
    public List<SingleStockPost> refreshTop10(){
        Top10SingleStockPost = getPopularArticles();
        return Top10SingleStockPost;
    }

    // 상위 10개 가져오는 코드
    @Override
    public List<SingleStockPost> getPopularArticles() {
        return em.createQuery("SELECT s FROM SingleStockPost s ORDER BY s.article.viewCount DESC", SingleStockPost.class)
                .setMaxResults(10) // 상위 10개만 가져오기
                .getResultList();
    }
}
