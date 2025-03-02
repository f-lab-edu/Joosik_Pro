package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) @RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void saveStock(Stock stock){
        stockRepository.save(stock);
    }

    public Stock findStock(Long id){
        return stockRepository.findStock(id);
    }


}
