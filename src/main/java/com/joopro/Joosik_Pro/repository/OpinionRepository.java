package com.joopro.Joosik_Pro.repository;


import com.joopro.Joosik_Pro.domain.Opinion;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class OpinionRepository {

    private final EntityManager em;

    // 댓글 저장
    public void save(Opinion opinion) {
        em.persist(opinion);
    }

    // opinion Id로 찾기
    public Opinion findById(Long id){
        return em.find(Opinion.class, id);
    }

    public List<Opinion> findOpinionByMemberId(Long memberId) {
        return em.createQuery("SELECT o FROM Opinion o WHERE o.member.id = :memberId", Opinion.class)
                .setParameter("memberId", memberId)
                .getResultList();
    }




}
