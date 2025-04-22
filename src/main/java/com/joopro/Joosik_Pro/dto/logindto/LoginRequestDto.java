package com.joopro.Joosik_Pro.dto.logindto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String name;
    private String password;
}