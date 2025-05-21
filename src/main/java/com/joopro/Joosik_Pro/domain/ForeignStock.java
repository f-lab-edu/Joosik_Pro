package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foreign_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForeignStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "stock_id")
    private Stock stock;

    private double 현재가;  // 현재가
    private double 전일종가;  // 전일종가
    private double 거래량; // 거래량

    @Builder
    public ForeignStock(Stock stock, double 현재가, double 전일종가, double 거래량) {
        this.stock = stock;
        this.현재가 = 현재가;
        this.전일종가 = 전일종가;
        this.거래량 = 거래량;
    }
}
