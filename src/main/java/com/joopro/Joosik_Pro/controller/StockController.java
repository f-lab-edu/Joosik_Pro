package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.stockdto.StockDetailDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDto;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    //주식 전체 리스트 반환
    @GetMapping("/api/stocks")
    public Result stocks(){
        List<Stock> stockList = stockService.getstocks();
        List<StockDto> stockDtoList = stockList.stream()
                .map(m -> new StockDto(m.getCompany_name(), m.getMember_number(), m.getArticle_number(), m.getTicker(), m.getSector()))
                .toList();
        return new Result("success", stockDtoList);
    }

    //주식 이름으로 찾기
    @GetMapping("/api/stocks/name")
    public Result findStockByName(@RequestParam String stockName){
        Stock stock = stockService.findStockByName(stockName);
        StockDto stockDto = new StockDto(stock.getCompany_name(), stock.getMember_number(), stock.getArticle_number(), stock.getTicker(), stock.getSector());
        return new Result("success", stockDto);
    }

    //stock 추가 API는 stock 데이터 받아오면 만들 예정


    //주식 상세 정보 조회
    @GetMapping("/api/stocks/{id}")
    public StockDetailDto stockDetailDto(@PathVariable("id") Long id){
        Stock stock = stockService.findStockById(id);
        StockDetailDto stockDetailDto = new StockDetailDto();
        // stock 데이터 받아온 뒤 재개


        return stockDetailDto;
    }




}
