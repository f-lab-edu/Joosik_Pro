package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.stockdto.StockDetailDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDto;
import com.joopro.Joosik_Pro.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;

    @Transactional
    public void saveStock(Stock stock){
        stockRepository.save(stock);
    }

    public StockDetailDto findStockById(Long id){
        Stock stock = stockRepository.findStockById(id);



        StockDetailDto stockDetailDto = new StockDetailDto();
        return stockDetailDto;
    }

    public Stock findStockByIdReturnEntity(Long id){
        return stockRepository.findStockById(id);
    }

    public StockDto findStockByCompanyName(String name){
        Stock stock = stockRepository.findStockByCompanyName(name);
        StockDto stockDto = StockDto.builder()
                .companyName(stock.getCompanyName())
                .memberNumber(stock.getMemberNumber())
                .articleNumber(stock.getArticleNumber())
                .ticker(stock.getTicker())
                .sector(stock.getSector())
                .build();
        return stockDto;
    }

    public Stock findStockByCompanyNameReturnEntity(String name){
        return stockRepository.findStockByCompanyName(name);
    }


    public List<StockDto> getAllStocks() {
        List<Stock> stockList = stockRepository.findAllStocks();
        List<StockDto> stockDtoList = stockList.stream()
                .map(m -> StockDto.builder()
                        .companyName(m.getCompanyName())
                        .memberNumber(m.getMemberNumber())
                        .articleNumber(m.getArticleNumber())
                        .ticker(m.getTicker())
                        .sector(m.getSector())
                        .build()
                ).toList();
        return stockDtoList;
    }

}
