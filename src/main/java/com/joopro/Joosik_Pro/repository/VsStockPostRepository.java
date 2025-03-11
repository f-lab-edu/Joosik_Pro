package com.joopro.Joosik_Pro.repository;


import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class VsStockPostRepository {

    private final EntityManager em;

    public void save(VsStockPost post){
        em.persist(post);
    }

    public VsStockPost findByVsStockPostId(Long id){
        return em.find(VsStockPost.class, id);
    }


    public List<VsStockPost> findVsStockPostByBelongSinglePostId(Long stockId){
        String jpql = "SELECT v FROM VsStockPost v WHERE v.stock1.id = :stockId OR v.stock2.id = :stockId";
        TypedQuery<VsStockPost> query = em.createQuery(jpql, VsStockPost.class);
        query.setParameter("stockId", stockId);
        return query.getResultList();
    }

    public List<VsStockPost> findAll() {
        return em.createQuery("SELECT v FROM VsStockPost v", VsStockPost.class)
                .getResultList();
    }

    public List<VsStockPost> findBySimilarContent(String keyword) {
        return em.createQuery("SELECT v FROM VsStockPost v WHERE v.article.content LIKE :keyword", VsStockPost.class)
                .setParameter("keyword", "%" + keyword + "%")
                .getResultList();
    }

    public void deleteById(Long id){
        em.remove(findByVsStockPostId(id));
    }

    public void increaseViewCount(Long id) {
        em.createQuery("UPDATE VsStockPost v SET v.article.viewCount = v.article.viewCount + 1 WHERE v.id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public List<VsStockPost> getPopularArticles() {
        return em.createQuery("SELECT v FROM VsStockPost v ORDER BY v.article.viewCount DESC", VsStockPost.class)
                .setMaxResults(10) // 상위 10개만 가져오기
                .getResultList();
    }

}
