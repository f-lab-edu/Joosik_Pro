package com.joopro.Joosik_Pro.dto.opiniondto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReturnOpinionDto {

    private Long memberId;
    private Long articleId;
    private Long OpinionId;

}
