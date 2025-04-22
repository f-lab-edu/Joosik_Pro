package com.joopro.Joosik_Pro.dto.logindto;

import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
    private boolean success;
    private String message;
    private MemberDtoResponse member;
}