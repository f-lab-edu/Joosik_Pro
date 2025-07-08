package com.joopro.Joosik_Pro.repository.FirstComeEventRepository;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.StockMembership;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void saveAll(List<FirstComeEventParticipation> participation) {
        for (int i = 0; i < participation.size(); i++) {
            em.persist(participation.get(i));
            if (i % 100 == 0) {
                em.flush();
                em.clear();
            }
        }
        em.flush();
        em.clear();

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
