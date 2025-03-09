package com.joopro.Joosik_Pro.repository;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockMemberShipRepository {

    private final EntityManager em;

    public void makeStockMemberShip(StockMembership stockMembership){
        em.persist(stockMembership);
    }

    public StockMembership findStockMemberShip(Long id){
        return em.find(StockMembership.class, id);
    }

    public List<StockMembership> findSubscribeMembers(Stock stock){
        return em.createQuery("select sm from StockMembership sm join fetch sm.member where sm.stock = :stock", StockMembership.class)
                .setParameter("stock", stock)
                .getResultList();

    }

    public List<StockMembership> findSubscribeStock(Member member){
        return em.createQuery("select sm from StockMembership sm join fetch sm.stock where sm.member = :member", StockMembership.class)
                .setParameter("member", member)
                .getResultList();
    }




}
