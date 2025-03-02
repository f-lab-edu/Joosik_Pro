package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Stock {

    @Id @GeneratedValue
    @Column(name = "stock_id")
    private int id;

    @OneToMany(mappedBy = "stock")
    private List<StockMembership> memberships = new ArrayList<>();

    @OneToMany(mappedBy = "stock", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SingleStockPost> singleStockPosts = new ArrayList<>();

    @OneToMany(mappedBy = "stock1", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VsStockPost> vsStockPosts1 = new ArrayList<>();

    @OneToMany(mappedBy = "stock2", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VsStockPost> vsStockPosts2 = new ArrayList<>();

    private String company_name;

    private int member_number;

    private int article_number;

    private String ticker;

    private String sector;

    // 생성 메서드
    public static Stock createStock(String companyname, String ticker, String sector){
        Stock stock = new Stock();
        stock.setCompany_name(companyname);
        stock.setTicker(ticker);
        return stock;
    }

}
