package com.joopro.Joosik_Pro.controller.stockcontroller;

import com.joopro.Joosik_Pro.domain.DomesticStock;
import com.joopro.Joosik_Pro.service.StockService.DomesticStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/DomesticStock")
@RequiredArgsConstructor
public class DomesticStockController {

    private final DomesticStockService domesticStockService;

    @GetMapping("/{symbol}")
    public void getStock(@PathVariable String symbol) {
        DomesticStock domesticStock = domesticStockService.FindDomesticStock(symbol);
        log.info("success");
        log.info("domesticStock : {}", domesticStock);

    }

}
