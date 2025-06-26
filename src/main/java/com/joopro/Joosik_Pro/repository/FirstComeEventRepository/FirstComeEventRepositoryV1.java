package com.joopro.Joosik_Pro.repository.FirstComeEventRepository;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.StockMembership;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FirstComeEventRepositoryV1 {

    private final EntityManager em;

    // stockMemberShip 저장
    public void makefirstcomeevent(FirstComeEventParticipation FirstComeEventParticipation){
        em.persist(FirstComeEventParticipation);
    }

    // stockId로 모든 참여자 조회
    public List<FirstComeEventParticipation> findAllByStockId(Long stockId) {
        return em.createQuery(
                        "SELECT p FROM FirstComeEventParticipation p WHERE p.stock.id = :stockId ORDER BY p.participateOrder ASC",
                        FirstComeEventParticipation.class)
                .setParameter("stockId", stockId)
                .getResultList();
    }

}
