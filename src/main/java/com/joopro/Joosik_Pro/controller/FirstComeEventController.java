package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.dto.FirstComeEventParticipationDto;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class FirstComeEventController {

    private final FirstComeEventService firstComeEventService;

    @PostMapping("/api/firstcome")
    public Result<Boolean> members(
            @RequestParam Long stockId,
            @RequestParam Long memberId
    ){
        boolean result = firstComeEventService.tryParticipate(stockId, memberId);
        return Result.ok(result);
    }

    @GetMapping("/api/firstcome/participants")
    public Result<List<FirstComeEventParticipationDto>> getParticipants(@RequestParam Long stockId) {
        List<FirstComeEventParticipationDto> participants = firstComeEventService.getParticipationDtoList(stockId);
        return Result.ok(participants);
    }


    @GetMapping("/api/firstcome/hasParticipated")
    public Result<Boolean> hasParticipated(
            @RequestParam Long stockId,
            @RequestParam Long memberId
    ) {
        boolean participated = firstComeEventService.hasParticipated(stockId, memberId);
        return Result.ok(participated);
    }

    @GetMapping("/api/firstcome/count")
    public Result<Integer> getCurrentCount(@RequestParam Long stockId) {
        int count = firstComeEventService.getCurrentCount(stockId);
        return Result.ok(count);
    }

    @GetMapping("/api/firstcome/participantIds")
    public Result<List<Long>> getParticipantIds(@RequestParam Long stockId) {
        List<Long> participantIds = firstComeEventService.getParticipants(stockId);
        return Result.ok(participantIds);
    }

}
