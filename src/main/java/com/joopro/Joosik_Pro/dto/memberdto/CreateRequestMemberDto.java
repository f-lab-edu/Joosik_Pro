package com.joopro.Joosik_Pro.dto.memberdto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateRequestMemberDto {
    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    private String email;
}
