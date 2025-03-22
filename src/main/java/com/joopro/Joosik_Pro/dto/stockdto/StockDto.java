package com.joopro.Joosik_Pro.dto.stockdto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StockDto {
    private String companyName;
    private int memberNumber;
    private int articleNumber;
    private String ticker;
    private String sector;

    @Builder
    public StockDto(String companyName, int memberNumber, int articleNumber, String ticker, String sector){
        this.companyName = companyName;
        this.memberNumber = memberNumber;
        this.articleNumber = articleNumber;
        this.ticker = ticker;
        this.sector = sector;
    }

}
