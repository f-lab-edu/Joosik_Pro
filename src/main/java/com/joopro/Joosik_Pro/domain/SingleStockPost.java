package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA용 기본 생성자, 외부에서 사용 방지
public class SingleStockPost {

    @Id @GeneratedValue
    @Column(name = "single_stock_post_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "single_article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    // 연관관계 편의 메서드
    public void setArticle(Article article){
        this.article = article;
        article.assignSingleStockPost(this);
    }

    // 연관관계 편의 메서드
    public void setStock(Stock stock){
        this.stock = stock;
        stock.addSingleStockPost(this);
    }


}
