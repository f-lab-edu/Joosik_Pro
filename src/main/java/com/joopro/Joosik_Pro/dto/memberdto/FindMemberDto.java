package com.joopro.Joosik_Pro.dto.memberdto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Repository단 @RequestBody에서 사용할 생성자입니다.
public class FindMemberDto {

    @NotBlank(message = "회원 아이디는 필수입니다.")
    Long memberId;

}
