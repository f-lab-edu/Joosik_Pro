package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateVsStockPostDto {

    private String content;
    private String firstStockName;
    private String secondStockName;


}
