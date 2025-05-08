package com.joopro.Joosik_Pro.dto.logindto;

import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class LoginResponseDto {
    @Getter
    private boolean success;
    @Getter
    private String message;
    private MemberDtoResponse member;

    @Builder
    public LoginResponseDto(boolean success, String message, MemberDtoResponse member){
        this.success = success;
        this.message = message;
        this.member = member;
    }

    public static LoginResponseDto success(MemberDtoResponse member){
        return LoginResponseDto.builder()
                .success(true)
                .message("로그인 성공")
                .member(member)
                .build();
    }

    public static LoginResponseDto fail(){
        return LoginResponseDto.builder()
                .success(false)
                .message("비밀번호가 틀렸습니다.")
                .member(null)
                .build();
    }

}