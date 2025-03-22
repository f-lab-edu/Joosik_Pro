package com.joopro.Joosik_Pro.dto.opiniondto;

import lombok.Builder;

public class ReturnOpinionDto {

    private Long memberId;
    private Long articleId;
    private Long opinionId;

    @Builder
    public ReturnOpinionDto(Long memberId, Long articleId, Long opinionId){
        this.memberId = memberId;
        this.articleId = articleId;
        this.opinionId = opinionId;
    }

}
