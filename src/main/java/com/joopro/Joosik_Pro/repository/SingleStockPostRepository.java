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

    public void delete(Long id) {
        SingleStockPost post = findById(id);
        if (post != null) {
            em.remove(post);
        }
    }

}
