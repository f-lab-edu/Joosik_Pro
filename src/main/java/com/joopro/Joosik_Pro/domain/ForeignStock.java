package com.joopro.Joosik_Pro.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "foreign_stock")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ForeignStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double base;  // 전일 종가
    private double last;  // 현재가
    private int sign;     // 대비 기호
    private double diff;  // 대비
    private double rate;  // 등락율
    private long tvol;    // 거래량
    private long tamt;    // 거래대금

    public ForeignStock(double base, double last, int sign, double diff, double rate, long tvol, long tamt) {
        this.base = base;
        this.last = last;
        this.sign = sign;
        this.diff = diff;
        this.rate = rate;
        this.tvol = tvol;
        this.tamt = tamt;
    }

}
