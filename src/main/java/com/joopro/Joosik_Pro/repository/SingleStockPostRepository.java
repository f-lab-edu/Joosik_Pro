package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.SingleStockPost;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SingleStockPostRepository {

    private final EntityManager em;

    public void save(SingleStockPost post){
        if(post.getId() == null){
            em.persist(post);
        }else{
            em.merge(post);
        }
    }

    public SingleStockPost findById(Long id){
        return em.find(SingleStockPost.class, id);
    }

    public List<SingleStockPost> findByStockId(Long stockId) {
        return em.createQuery("SELECT s FROM SingleStockPost s WHERE s.stock.id = :stockId", SingleStockPost.class)
                .setParameter("stockId", stockId)
                .getResultList();
    }

    public List<SingleStockPost> findBySimilarContent(String keyword) {
        return em.createQuery("SELECT s FROM SingleStockPost s WHERE s.article.content LIKE :keyword", SingleStockPost.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    public List<SingleStockPost> findAllSingleStockPost(){
        return em.createQuery("SELECT s from SingleStockPost s", SingleStockPost.class)
                .getResultList();
    }

    public void deleteById(Long id) {
        SingleStockPost post = findById(id);
        if (post != null) {
            em.remove(post);
        }
    }

    public void increaseViewCount(Long id) {
        em.createQuery("UPDATE SingleStockPost s SET s.article.viewCount = s.article.viewCount + 1 WHERE s.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public List<SingleStockPost> getPopularArticles() {
        return em.createQuery("SELECT s FROM SingleStockPost s ORDER BY s.article.viewCount DESC", SingleStockPost.class)
                .setMaxResults(10) // 상위 10개만 가져오기
                .getResultList();
    }

}
