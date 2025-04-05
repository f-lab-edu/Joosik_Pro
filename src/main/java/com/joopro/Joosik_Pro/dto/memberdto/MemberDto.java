package com.joopro.Joosik_Pro.dto.memberdto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
public class MemberDto {
    private String name;

    @Builder
    public MemberDto(String name){
        this.name = name;
    }

}
