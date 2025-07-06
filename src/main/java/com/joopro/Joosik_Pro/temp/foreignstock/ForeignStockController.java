package com.joopro.Joosik_Pro.temp.foreignstock;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ForeignStock")
@RequiredArgsConstructor
public class ForeignStockController {

    private final ForeignStockService foreignStockService;

    @GetMapping("/{symbol}")
    public ResponseEntity<ForeignStock> getStock(@PathVariable String symbol) {
        ForeignStock stock = foreignStockService.fetchForeignStock(symbol);
        return ResponseEntity.ok(stock);
    }
}

