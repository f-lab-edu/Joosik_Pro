package com.joopro.Joosik_Pro.dto.stockdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockDto {
    private String company_name;
    private int member_number;
    private int article_number;
    private String ticker;
    private String sector;
}
