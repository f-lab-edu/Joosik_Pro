package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "domestic_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DomesticStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private Double 현재가;
    private Double 최고가;
    private Double 최저가;
    private Double 상한가;  // 주식 상한가
    private Double 하한가;  // 주식 하한가

    private Double from250HighPrice; // 250일 최고가
    private Double from250LowPrice;  // 250일 최저가
    private Double fromYearHighPrice; // 주식 연중 최고가
    private Double fromYearLowPrice;  // 주식 연중 최저가
    private Double from52wHighPrice;  // 52주일 최고가
    private Double from52wLowPrice;  // 52주일 최저가

    private Double per;
    private Double pbr;
    private Double eps;

    @Builder
    public DomesticStock(Double 현재가, Double 최고가, Double 최저가, Double 상한가, Double 하한가, Double from250HighPrice, Double from250LowPrice, Double fromYearHighPrice, Double fromYearLowPrice, Double from52wHighPrice, Double from52wLowPrice, Double per, Double pbr, Double eps) {
        this.현재가 = 현재가;
        this.최고가 = 최고가;
        this.최저가 = 최저가;
        this.상한가 = 상한가;
        this.하한가 = 하한가;
        this.from250HighPrice = from250HighPrice;
        this.from250LowPrice = from250LowPrice;
        this.fromYearHighPrice = fromYearHighPrice;
        this.fromYearLowPrice = fromYearLowPrice;
        this.from52wHighPrice = from52wHighPrice;
        this.from52wLowPrice = from52wLowPrice;
        this.per = per;
        this.pbr = pbr;
        this.eps = eps;
    }



}
