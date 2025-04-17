package com.joopro.Joosik_Pro.dto.opiniondto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateOpinionDto {

    private Long memberId;
    private Long articleId;

    @Getter
    @NotNull
    private String comment;

    @Getter
    private Long parentOpinionId;

}
