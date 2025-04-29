package com.joopro.Joosik_Pro.service.StockService;

import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.stockdto.StockDetailDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
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

    // Stock ID로 Entity 반환
    public Stock findStockByIdReturnEntity(Long id){
        return stockRepository.findStockById(id);
    }

    public StockDtoResponse findStockByCompanyName(String name){
        Stock stock = stockRepository.findStockByCompanyName(name);
        StockDtoResponse stockDtoResponse = StockDtoResponse.of(stock);
        return stockDtoResponse;
    }

    // Stock Name으로 Entity 반환
    public Stock findStockByCompanyNameReturnEntity(String name){
        return stockRepository.findStockByCompanyName(name);
    }

    public List<StockDtoResponse> getAllStocks() {
        List<Stock> stockList = stockRepository.findAllStocks();
        List<StockDtoResponse> stockDtoResponseList = stockList.stream()
                .map(s -> StockDtoResponse.of(s))
                .toList();
        return stockDtoResponseList;
    }

}
