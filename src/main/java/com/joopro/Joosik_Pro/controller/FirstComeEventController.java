package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
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
    private final FirstComeEventRepositoryV1 firstComeEventRepositoryV1;

    @PostMapping("/api/firstcome")
    public Result<Boolean> members(
            @RequestParam Long stockId,
            @RequestParam Long memberId
    ){
        boolean result = firstComeEventService.tryParticipate(stockId, memberId);
        return Result.ok(result);
    }

    @GetMapping("/api/firstcome/participants")
    public Result<List<FirstComeEventParticipation>> getParticipants(@RequestParam Long stockId) {
        List<FirstComeEventParticipation> participants = firstComeEventRepositoryV1.findAllByStockId(stockId);
        return Result.ok(participants);
    }

}
