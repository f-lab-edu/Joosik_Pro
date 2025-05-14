package com.joopro.Joosik_Pro.dto.postdto;

import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import lombok.Builder;
import lombok.Getter;

// 직렬화(Jackson)가 사용할 @Getter
@Getter
public class VsStockPostDtoResponse {

    private String firstStockName;
    private String secondStockName;
    private String memberName;
    private String content;

    @Builder
    public VsStockPostDtoResponse(String firstStockName, String secondStockName, String memberName, String content){
        this.firstStockName = firstStockName;
        this.secondStockName = secondStockName;
        this.memberName = memberName;
        this.content = content;
    }

    public static VsStockPostDtoResponse of(VsStockPost vsStockPost){
        return VsStockPostDtoResponse.builder()
                .firstStockName(vsStockPost.getStock1().getCompanyName())
                .secondStockName(vsStockPost.getStock2().getCompanyName())
                .memberName(vsStockPost.getMember().getName())
                .content(vsStockPost.getContent())
                .build();
    }

}
