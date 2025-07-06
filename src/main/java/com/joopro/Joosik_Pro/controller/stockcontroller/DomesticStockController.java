package com.joopro.Joosik_Pro.controller.stockcontroller;

import com.joopro.Joosik_Pro.domain.DomesticStock;
import com.joopro.Joosik_Pro.service.StockService.DomesticStockLiveService;
import com.joopro.Joosik_Pro.service.StockService.DomesticStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.Path;

@Slf4j
@RestController
@RequestMapping("/api/DomesticStock")
@RequiredArgsConstructor
public class DomesticStockController {

    private final DomesticStockService domesticStockService;
    private final DomesticStockLiveService domesticStockLiveService;

    @GetMapping("/{symbol}")
    public void getStock(@PathVariable String symbol) {
        DomesticStock domesticStock = domesticStockService.FindDomesticStock(symbol);
        log.info("success");
        log.info("domesticStock : {}", domesticStock);
    }


    @PostMapping("/startstockservice/{symbol}")
    public void startStock(@PathVariable String symbol){
        String isinCD = domesticStockService.extractedISIN(symbol);
        String stockCode = isinCD.substring(3, 9);
        log.info("isinCD 코드 등록 성공 : {}", stockCode);
        domesticStockLiveService.startLiveStream(stockCode);
    }

}
