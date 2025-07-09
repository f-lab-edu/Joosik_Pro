package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ParticipantDto {
    private Long stockId;
    private Long memberId;
    private int order;
}