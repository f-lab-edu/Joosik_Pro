package com.joopro.Joosik_Pro.dto.memberdto;

import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor // Repository단 @RequestBody에서 사용할 생성자입니다.
@Getter
public class CreateRequestMemberDto {

    @NotNull(message = "회원 이름은 필수입니다.")
    private String username;

    @NotNull(message = "이메일은 필수입니다.")
    private String email;

}
