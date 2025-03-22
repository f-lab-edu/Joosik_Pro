package com.joopro.Joosik_Pro.dto.stockdto;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FindStockDto {

    @NotNull
    Long stockId;
}
