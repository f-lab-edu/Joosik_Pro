package com.joopro.Joosik_Pro.repository;


import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StockRepository {

    private final EntityManager em;

    // stock 저장하기
    public void save(Stock stock){
        em.persist(stock);
    }

    // stockID로 찾기
    public Stock findStockById(Long id){
        return em.find(Stock.class, id);
    }

    // 모든 Stock 찾기
    public List<Stock> findAllStocks(){
        return em.createQuery("select s from Stock  s", Stock.class)
                .getResultList();
    }

    // 회사이름으로 Stock 찾기
    public Stock findStockByCompanyName(String name){
        return em.createQuery("select s from Stock s where s.company_name = :name", Stock.class)
                .setParameter("name", name)
                .getSingleResult();
    }


}
