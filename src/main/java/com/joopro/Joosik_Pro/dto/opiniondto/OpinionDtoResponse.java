package com.joopro.Joosik_Pro.dto.opiniondto;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import jakarta.persistence.*;
import lombok.Builder;

public class OpinionDtoResponse {

    private String memberName;
    private String opinionContent;
    private long like_sum;
    private long dislike_sum;

    @Builder
    private OpinionDtoResponse(Opinion opinion){
        this.memberName = opinion.getMember().getName();
        this.opinionContent = opinion.getComment();
        this.like_sum = opinion.getLike_sum();
        this.dislike_sum = opinion.getDislike_sum();
    }

    public static OpinionDtoResponse of(Opinion opinion){
        return OpinionDtoResponse.builder()
                .opinion(opinion)
                .build();
    }

}
