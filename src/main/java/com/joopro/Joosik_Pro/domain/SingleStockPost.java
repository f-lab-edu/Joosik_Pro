package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class SingleStockPost {

    @Id @GeneratedValue
    @Column(name = "single_stock_post_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "single_article_id")
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "single_stock_id")
    private Stock stock;

    public SingleStockPost createSingleStockPost(Article article, Stock stock){
        SingleStockPost singleStockPost = new SingleStockPost();
        singleStockPost.setArticle(article);
        stock.addSingleStockPost(singleStockPost);
        return singleStockPost;
    }


}
