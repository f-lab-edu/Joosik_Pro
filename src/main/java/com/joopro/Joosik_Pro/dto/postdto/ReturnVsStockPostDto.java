package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class ReturnVsStockPostDto {

    private String firstStockName;
    private String secondStockName;
    private String memberName;
    private String content;

    @Builder
    public ReturnVsStockPostDto(String firstStockName, String secondStockName, String memberName, String content){
        this.firstStockName = firstStockName;
        this.secondStockName = secondStockName;
        this.memberName = memberName;
        this.content = content;
    }

}
