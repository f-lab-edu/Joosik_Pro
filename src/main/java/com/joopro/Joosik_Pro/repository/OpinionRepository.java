package com.joopro.Joosik_Pro.repository;


import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OpinionRepository {

    private final EntityManager em;

    public void save(Opinion opinion) {
        em.persist(opinion);
    }

   public Opinion findById(Long id){
        return em.find(Opinion.class, id);
   }

    public List<Opinion> findByMemberId(Long memberId) {
        return em.createQuery("SELECT o FROM Opinion o WHERE o.member.id = :memberId", Opinion.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }




}
