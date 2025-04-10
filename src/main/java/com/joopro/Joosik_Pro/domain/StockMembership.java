package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
public class StockMembership {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Setter
    private boolean isActive;

    @Builder
    public StockMembership(Member member, Stock stock){
        this.member = member;
        this.stock = stock;
        this.isActive = true;
    }

    //생성 메서드
    public static StockMembership createStockMemberShip(Member member, Stock stock){
        StockMembership stockMembership = StockMembership.builder()
                .member(member)
                .stock(stock)
                .build();
        stockMembership.setMember(member);
        stockMembership.setStock(stock);
        stock.addMemberNumber();
        return stockMembership;
    }

    // 연관관계 편의 메서드
    private void setMember(Member member){
        this.member = member;
        member.addStockMemberShip(this);
    }

    // 연관관계 편의 메서드
    public void setStock(Stock stock){
        this.stock = stock;
        stock.addStockMemberShip(this);
    }

    // 멤버십 취소
    public void cancel(){
        this.setActive(false);
    }

}
