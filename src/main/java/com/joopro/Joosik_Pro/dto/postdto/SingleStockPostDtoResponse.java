package com.joopro.Joosik_Pro.dto.postdto;

import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.dto.opiniondto.OpinionDtoResponse;
import lombok.Builder;
import lombok.Getter;

// 직렬화(Jackson)가 사용할 @Getter
@Getter
public class SingleStockPostDtoResponse {
    private String stockName;
    private String memberName;
    private String content;

    @Builder
    public SingleStockPostDtoResponse(String stockName, String memberName, String content){
        this.stockName = stockName;
        this.memberName = memberName;
        this.content = content;
    }

    public static SingleStockPostDtoResponse of(SingleStockPost singleStockPost){
        return SingleStockPostDtoResponse.builder()
                .stockName(singleStockPost.getStock().getCompanyName())
                .memberName(singleStockPost.getMember().getName())
                .content(singleStockPost.getContent())
                .build();
    }

}
