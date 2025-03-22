package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

public class StockMemberShipDto {

    private String member;
    private String stock;

    @Builder
    public StockMemberShipDto(String member, String stock){
        this.member = member;
        this.stock = stock;
    }

}
