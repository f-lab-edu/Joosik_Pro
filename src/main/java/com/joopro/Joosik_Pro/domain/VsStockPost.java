package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity @Getter
public class VsStockPost {

    @Id @GeneratedValue
    @Column(name = "vs_stock_post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock1_id")
    private Stock stock1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock2_id")
    private Stock stock2;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vs_article_id")
    private Article article;

    // 연관관계 편의 메서드
    public void setArticle(Article article){
        this.article = article;
        article.assignVsStockPost(this);
    }

    // 연관관계 편의 메서드
    public void setStocks(Stock stock1, Stock stock2){
        this.stock1 = stock1;
        this.stock2 = stock2;
    }

}
