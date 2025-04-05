package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.service.DomesticStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/DomesticStock")
@RequiredArgsConstructor
public class DomesticStockController {

    private final DomesticStockService domesticStockServiceService;

    @GetMapping("/{symbol}")
    public void getStock(@PathVariable String symbol) {
        domesticStockServiceService.fetchDomesticStock(symbol);

    }

}
