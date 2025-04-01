package com.joopro.Joosik_Pro.dto.postdto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // Repository에서 @RequestBody에서 사용할 생성자입니다.
public class CreateVsStockPostDto {
    private Long userId;
    private String content;
    private Long firstStockId;
    private Long secondStockId;

}
