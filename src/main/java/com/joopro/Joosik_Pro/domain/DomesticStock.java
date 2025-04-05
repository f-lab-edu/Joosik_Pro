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

    private String stckShrnIscd; // 주식 단축 종목코드
    private String rprsMrktKorName; // 대표 시장 한글 명
    private String bstpKorIsnm; // 업종 한글 종목명
    private String stckPrpr; // 주식 현재가
    private String stckOprc; // 주식 시가
    private String stckHgpr; // 주식 최고가
    private String stckLwpr; // 주식 최저가
    private String stckMxpr; // 주식 상한가
    private String stckLlam; // 주식 하한가
    private String stckSdpr; // 주식 기준가
    private String per; // PER
    private String pbr; // PBR
    private String eps; // EPS
    private String bps; // BPS
    private String w52Hgpr; // 52주 최고가
    private String w52Lwpr; // 52주 최저가
    private String d250Hgpr; // 250일 최고가
    private String d250Lwpr; // 250일 최저가
    private String stckDryyHgpr; // 연중 최고가
    private String stckDryyLwpr; // 연중 최저가
    private String cpfn; // 자본금

    @Builder
    public DomesticStock(String stckShrnIscd, String rprsMrktKorName, String bstpKorIsnm, String stckPrpr, String stckOprc, String stckHgpr, String stckLwpr, String stckMxpr, String stckLlam, String stckSdpr, String per, String pbr, String eps, String bps, String w52Hgpr, String w52Lwpr, String d250Hgpr, String d250Lwpr, String stckDryyLwpr, String stckDryyHgpr, String cpfn) {
        this.stckShrnIscd = stckShrnIscd;
        this.rprsMrktKorName = rprsMrktKorName;
        this.bstpKorIsnm = bstpKorIsnm;
        this.stckPrpr = stckPrpr;
        this.stckOprc = stckOprc;
        this.stckHgpr = stckHgpr;
        this.stckLwpr = stckLwpr;
        this.stckMxpr = stckMxpr;
        this.stckLlam = stckLlam;
        this.stckSdpr = stckSdpr;
        this.per = per;
        this.pbr = pbr;
        this.eps = eps;
        this.bps = bps;
        this.w52Hgpr = w52Hgpr;
        this.w52Lwpr = w52Lwpr;
        this.d250Hgpr = d250Hgpr;
        this.d250Lwpr = d250Lwpr;
        this.stckDryyLwpr = stckDryyLwpr;
        this.stckDryyHgpr = stckDryyHgpr;
        this.cpfn = cpfn;
    }

}
