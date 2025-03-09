package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReturnSingleStockPostDto {
    String stockName;
    String memberName;
    String content;

}
