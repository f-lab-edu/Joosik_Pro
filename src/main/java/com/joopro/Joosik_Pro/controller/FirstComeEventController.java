package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.service.FirstComeEventService.FirstComeEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
