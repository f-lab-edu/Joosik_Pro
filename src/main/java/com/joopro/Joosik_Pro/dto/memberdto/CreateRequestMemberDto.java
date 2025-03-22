package com.joopro.Joosik_Pro.dto.memberdto;

import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor // Repository에서 @RequestBody에서 사용할 생성자입니다.
public class CreateRequestMemberDto {

    @NotEmpty
    @Getter
    private String username;

    @NotEmpty
    private String password;

    @Getter
    private String email;

}
