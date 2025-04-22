package com.joopro.Joosik_Pro.dto.opiniondto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
public class CreateOpinionDto {

    private Long memberId;
    private Long articleId;

    @Getter
    @NotNull @Setter // 테스트용 @Setter
    private String comment;

    @Getter @Setter // 테스트용 @Setter
    private Long parentOpinionId;

}
