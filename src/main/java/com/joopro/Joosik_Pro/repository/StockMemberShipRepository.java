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

    public List<Member> findSubscribeMembers(Stock stock){
        return em.createQuery("select sm.member from StockMembership sm where sm.stock = :stock", Member.class)
                .setParameter("stock", stock)
                .getResultList();

    }

    public List<Stock> findSubscribeStock(Member member){
        return em.createQuery("select sm.stock from StockMembership sm where sm.member = :member", Stock.class)
                .setParameter("member", member)
                .getResultList();
    }




}
