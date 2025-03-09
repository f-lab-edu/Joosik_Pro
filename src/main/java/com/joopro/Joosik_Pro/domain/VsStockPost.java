package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
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

    public static VsStockPost createVsStockPost(Stock stock1, Stock stock2, Article article){
        VsStockPost vsStockPost = new VsStockPost();
        vsStockPost.setStock1(stock1);
        vsStockPost.setStock2(stock2);
        vsStockPost.setArticle(article);


        return vsStockPost;
    }

}
