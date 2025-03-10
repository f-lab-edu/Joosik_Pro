package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReturnVsStockPostDto {

    private String firstStockName;
    private String secondStockName;
    private String memberName;
    private String content;


}
