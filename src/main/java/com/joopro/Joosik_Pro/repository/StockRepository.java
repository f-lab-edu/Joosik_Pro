package com.joopro.Joosik_Pro.repository;


import com.joopro.Joosik_Pro.domain.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class StockRepository {

    private final EntityManager em;

    public void save(Stock stock){
        em.persist(stock);
    }

    public Stock findStock(Long id){
        return em.find(Stock.class, id);
    }

}
