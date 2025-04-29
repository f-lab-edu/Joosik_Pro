package com.joopro.Joosik_Pro.dto.logindto;

import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginResponseDto {
    private boolean success;
    private String message;
    private MemberDtoResponse member;
}