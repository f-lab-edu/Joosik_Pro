package com.joopro.Joosik_Pro.dto.opiniondto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CreateOpinionDto {

    @Getter
    @NotEmpty
    private String comment;

}
