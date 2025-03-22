package com.joopro.Joosik_Pro.dto.memberdto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindMemberDto {

    @NotNull
    Long memberId;
}
