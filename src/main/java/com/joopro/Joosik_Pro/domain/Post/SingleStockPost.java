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
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SingleStockPost extends Post {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private Stock stock;

    @Builder
    private SingleStockPost(String content, Member member, Stock stock) {
        super(content, member);
        this.stock = stock;
    }

    public static SingleStockPost makeSingleStockPost(String content, Member member, Stock stock){
        return SingleStockPost.builder()
                .content(content)
                .member(member)
                .stock(stock)
                .build();
    }

}