package com.joopro.Joosik_Pro.repository.FirstComeEventRepository;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.StockMembership;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FirstComeEventRepositoryV1 {

    private final EntityManager em;

    // stockMemberShip 저장
    public void makefirstcomeevent(FirstComeEventParticipation FirstComeEventParticipation){
        em.persist(FirstComeEventParticipation);
    }

}
