package com.joopro.Joosik_Pro.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FirstComeEventParticipation {

    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private int participateOrder; // 참여 순서

    @Builder
    public FirstComeEventParticipation(Stock stock, Member member, int participateOrder) {
        this.stock = stock;
        this.member = member;
        this.participateOrder = participateOrder;
    }

    // 생성 메서드
    public static FirstComeEventParticipation firstComeEventParticipation(Stock stock, Member member, int participateOrder){
        FirstComeEventParticipation firstComeEventParticipation = FirstComeEventParticipation.builder()
                .stock(stock)
                .member(member)
                .participateOrder(participateOrder)
                .build();
        firstComeEventParticipation.setStock(stock);
        firstComeEventParticipation.setMember(member);
        return firstComeEventParticipation;
    }

    private void setStock(Stock stock) {
        this.stock = stock;
        stock.addEventParticipant(this);
    }

    private void setMember(Member member){
        this.member = member;
        member.addEventParticipation(this);
    }

}
