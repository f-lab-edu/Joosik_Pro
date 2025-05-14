package com.joopro.Joosik_Pro.domain;

import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
public class Stock {

    @Id @GeneratedValue
    @Column(name = "stock_id")
    private Long id;

    @OneToMany(mappedBy = "stock")
    private List<StockMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "stock", orphanRemoval = true)
    private List<SingleStockPost> singleStockPosts = new ArrayList<>();

    private String companyName;

    private int memberNumber;

    private int articleNumber;

    private String ticker;

    private String sector;

    // 생성 메서드
    @Builder
    public Stock(String companyName, String ticker, String sector){
        this.companyName = companyName;
        this.ticker = ticker;
        this.sector = sector;
        this.memberNumber = 0;
        this.articleNumber = 0;
    }

    // stockMemberShip을 가입할 때 호출하는 해당 주식 소속 회원 수 증가 메서드
    public void addMemberNumber(){
        this.memberNumber++;
    }

    // SingleStockPost에서 사용할 메서드 singleStockPosts 리스트에 추가
    public void addSingleStockPost(SingleStockPost singleStockPost){
        singleStockPosts.add(singleStockPost);
    }

    // StockMemberShip에서 사용할 메서드 memberships리스트에 추가
    public void addStockMemberShip(StockMembership stockMembership){
        memberships.add(stockMembership);
    }

    public void incrementArticleNumber() {
        this.articleNumber++;
    }

}
