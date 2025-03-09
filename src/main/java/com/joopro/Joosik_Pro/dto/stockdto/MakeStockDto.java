package com.joopro.Joosik_Pro.dto.stockdto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MakeStockDto {
    @NotNull
    private String companyName;
    private String ticker;
    private String sector;
}
