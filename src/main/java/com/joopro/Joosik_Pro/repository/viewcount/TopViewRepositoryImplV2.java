package com.joopro.Joosik_Pro.repository.viewcount;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * cache에 값이 있다면 increaseViewcount로 조회수 올리기 없다면 직접 찾아서 조회수 올리기
 * Service에서 1시간 후에 데이터베이스에 조회수 연동
 * 30분에 한번씩 sorCacheByViewCount로 조회수 기준 내림차순 정렬
 *
 */
@Repository
@RequiredArgsConstructor
public class TopViewRepositoryImplV2 implements TopViewRepository{

    private final EntityManager em;
    private Map<Long, Long> cache = new LinkedHashMap<>();

    // viewCount cache에 있는지 확인하고 있다면 cache에서 증가시키는 로직
    @Override
    public void increaseViewCount(Long postId){
        Long currentCount = cache.get(postId);
        if(cache.get(postId) != null){
            cache.put(postId, currentCount + 1);
        }else{
            SingleStockPost post = em.find(SingleStockPost.class, postId);
            Article article = post.getArticle();
            article.increaseViewCount(1L);
        }
    }

    @Override
    // 데이터베이스에 연동
    public void updateViewCountsToDB(){
        for(Map.Entry<Long, Long> entry : cache.entrySet()) {
            SingleStockPost post = em.find(SingleStockPost.class, entry.getKey());
            if (post != null) {
                post.getArticle().increaseViewCount(entry.getValue());
            }
        }
        cache.clear();
    }

    // cache에 있는 데이터를 value(조회수) 기준으로 내림차순 정렬하여 반환
    public void sortCacheByViewCount() {
        cache = cache.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    // cache에 데이터 넣기
    public void initializeCache(){
        cache = getPopularArticles().stream()
                .collect(Collectors.toMap(
                        SingleStockPost::getId,
                        post -> post.getArticle().getViewCount(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Override
    // 상위 10개 가져오는 코드
    public List<SingleStockPost> getPopularArticles() {
        return em.createQuery("SELECT s FROM SingleStockPost s ORDER BY s.article.viewCount DESC", SingleStockPost.class)
                .setMaxResults(100) // 상위 100개만 가져오기
                .getResultList();
    }

}