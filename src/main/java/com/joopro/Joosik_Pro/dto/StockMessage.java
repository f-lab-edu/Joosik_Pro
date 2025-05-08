package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Data
@AllArgsConstructor
public class StockMessage {
    private String code;
    private String name;
    private long price;
    private long volume;
    private String timestamp;
}
