package com.joopro.Joosik_Pro.dto.stockdto;

import com.joopro.Joosik_Pro.domain.Stock;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StockDtoResponse {
    private String companyName;
    private int memberNumber;
    private int articleNumber;
    private String ticker;
    private String sector;

    @Builder
    private StockDtoResponse(String companyName, int memberNumber, int articleNumber, String ticker, String sector){
        this.companyName = companyName;
        this.memberNumber = memberNumber;
        this.articleNumber = articleNumber;
        this.ticker = ticker;
        this.sector = sector;
    }

    public static StockDtoResponse of(Stock stock){
        return StockDtoResponse.builder()
                .companyName(stock.getCompanyName())
                .memberNumber(stock.getMemberNumber())
                .articleNumber(stock.getArticleNumber())
                .ticker(stock.getTicker())
                .sector(stock.getSector())
                .build();
    }

}
