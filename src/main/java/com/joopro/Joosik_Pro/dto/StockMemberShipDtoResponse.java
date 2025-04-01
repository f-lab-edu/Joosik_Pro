package com.joopro.Joosik_Pro.dto;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import lombok.Builder;

public class StockMemberShipDtoResponse {

    private String member;
    private String stock;

    @Builder
    private StockMemberShipDtoResponse(Member member, Stock stock){
        this.member = member.getName();
        this.stock = stock.getCompanyName();
    }

    public static StockMemberShipDtoResponse of(StockMembership stockMembership){
        return StockMemberShipDtoResponse.builder()
                .member(stockMembership.getMember())
                .stock(stockMembership.getStock())
                .build();
    }

}
