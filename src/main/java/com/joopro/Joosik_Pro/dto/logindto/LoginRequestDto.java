package com.joopro.Joosik_Pro.dto.logindto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequestDto {
    private String name;
    private String password;
}