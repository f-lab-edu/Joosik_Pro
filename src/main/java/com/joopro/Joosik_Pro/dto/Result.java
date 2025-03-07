package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Result <T>{
    private String message;
    private T data;
}
