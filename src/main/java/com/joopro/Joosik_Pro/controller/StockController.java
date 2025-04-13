package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.stockdto.StockDetailDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import com.joopro.Joosik_Pro.service.StockService.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    // 주식 전체 리스트 반환
    @GetMapping("/api/stocks")
    public Result<List<StockDtoResponse>> stocks() {
        List<StockDtoResponse> stockDtoResponseList = stockService.getAllStocks();
        return Result.ok(stockDtoResponseList);
    }

    // 주식 이름으로 찾기
    @GetMapping("/api/stocks/name")
    public Result<StockDtoResponse> findStockByName(@RequestParam String stockName) {
        StockDtoResponse stockDtoResponse = stockService.findStockByCompanyName(stockName);
        if (stockDtoResponse == null) {
            return Result.of(HttpStatus.NOT_FOUND, "Stock not found", null);
        }
        return Result.ok(stockDtoResponse);
    }

    // 주식 상세 정보 조회
    @GetMapping("/api/stocks/{id}")
    public Result<StockDetailDto> stockDetailDto(@PathVariable("id") Long id) {
        StockDetailDto stockDetailDto = stockService.findStockById(id);
        if (stockDetailDto == null) {
            return Result.of(HttpStatus.NOT_FOUND, "Stock details not found", null);
        }
        return Result.ok(stockDetailDto);
    }
}
