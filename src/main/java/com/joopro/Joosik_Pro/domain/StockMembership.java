package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class StockMembership {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private LocalDateTime date_subscribe;

    private boolean isActive;

    private void setMember(Member member){
        this.member = member;
        member.getMemberships().add(this);
    }

    public void setStock(Stock stock){
        this.stock = stock;
        stock.getMemberships().add(this);
    }

    //생성 메서드
    public static StockMembership createStockMemberShip(Member member, Stock stock){
        StockMembership stockMembership = new StockMembership();
        stockMembership.setMember(member);
        stockMembership.setStock(stock);
        stockMembership.setActive(true);
        int a = stock.getMember_number();
        stock.setMember_number(a+1);
        return stockMembership;
    }

    public void cancel(){
        this.setActive(false);
    }

}
