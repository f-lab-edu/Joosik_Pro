package com.joopro.Joosik_Pro.dto;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FirstComeEventParticipationDto {
    private Long id;
    private Long stockId;
    private Long memberId;
    private int participateOrder;

    @Builder
    private FirstComeEventParticipationDto(Long id, Long stockId, Long memberId, int participateOrder) {
        this.id = id;
        this.stockId = stockId;
        this.memberId = memberId;
        this.participateOrder = participateOrder;
    }

    public static FirstComeEventParticipationDto of(FirstComeEventParticipation entity) {
        return FirstComeEventParticipationDto.builder()
                .id(entity.getId())
                .stockId(entity.getStock().getId())
                .memberId(entity.getMember().getId())
                .participateOrder(entity.getParticipateOrder())
                .build();
    }
}

