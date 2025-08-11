package com.joopro.Joosik_Pro.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationMessage {
    private Long stockId;
    private Long memberId;
    private long startTimeNs; // 참여 시작 시간
}