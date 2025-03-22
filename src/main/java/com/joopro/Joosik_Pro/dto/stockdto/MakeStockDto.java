package com.joopro.Joosik_Pro.dto.stockdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


@Getter
public class MakeStockDto {
    @NotNull
    private String companyName;
    private String ticker;
    private String sector;
}
