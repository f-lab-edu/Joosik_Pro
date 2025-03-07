package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateSingleStockPostDto {
    private String content;
    private String stockName;

}
