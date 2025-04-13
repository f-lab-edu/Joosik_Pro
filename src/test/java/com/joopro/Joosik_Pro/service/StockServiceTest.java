package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.service.StockService.StockService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class StockServiceTest {

    @Autowired
    StockService stockService;

    @Test
    void saveStock() {
        //given
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");

        //when
        stockService.saveStock(stock);

        //then
        Assertions.assertThat(stock).isEqualTo(stockService.findStockById(stock.getId()));
        Assertions.assertThat(stock).isEqualTo(stockService.findStockByName("테슬라"));

    }

    @Test
    void getstocks() {
        //given
        Stock stock1 = Stock.createStock("테슬라", "TSLA", "IT");
        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");

        stockService.saveStock(stock1);
        stockService.saveStock(stock2);

        //when
        List<Stock> stockList = stockService.getstocks();

        //then
        Assertions.assertThat(stockList.size()).isEqualTo(2);
        Assertions.assertThat(stockList).contains(stock1, stock2);

    }
}