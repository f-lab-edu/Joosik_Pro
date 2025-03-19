package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class ReturnSingleStockPostDto {
    String stockName;
    String memberName;
    String content;

    @Builder
    public ReturnSingleStockPostDto(String stockName, String memberName, String content){
        this.stockName = stockName;
        this.memberName = memberName;
        this.content = content;
    }

}
