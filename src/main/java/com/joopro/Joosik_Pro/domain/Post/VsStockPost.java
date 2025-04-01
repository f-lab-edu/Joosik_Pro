package com.joopro.Joosik_Pro.domain.Post;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter @NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VsStockPost extends Post {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock1_id")
    private Stock stock1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock2_id")
    private Stock stock2;

    @Builder
    public VsStockPost(String content, Member member, Stock stock1, Stock stock2) {
        super(content, member);
        this.stock1 = stock1;
        this.stock2 = stock2;
    }

    public static VsStockPost makeVsStockPost(String content, Member member, Stock stock1, Stock stock2){
        return VsStockPost.builder()
                .content(content)
                .member(member)
                .stock1(stock1)
                .stock2(stock2)
                .build();
    }

}
