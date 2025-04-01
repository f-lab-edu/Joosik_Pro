package com.joopro.Joosik_Pro.dto.memberdto;

import com.joopro.Joosik_Pro.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MemberDtoResponse {
    private String name;

    @Builder
    private MemberDtoResponse(String name){
        this.name = name;
    }

    public static MemberDtoResponse of(Member member){
        return MemberDtoResponse.builder()
                .name(member.getName())
                .build();
    }

}
