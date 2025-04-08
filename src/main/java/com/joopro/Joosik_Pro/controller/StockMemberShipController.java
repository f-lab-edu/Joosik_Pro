package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.StockMemberShipDtoResponse;
import com.joopro.Joosik_Pro.dto.memberdto.FindMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import com.joopro.Joosik_Pro.dto.stockdto.FindStockDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import com.joopro.Joosik_Pro.service.StockMemberShipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockMemberShipController {

    private final StockMemberShipService stockMemberShipService;

    // Stock 팀 등록
    @PostMapping("/api/membership")
    public Result<StockMemberShipDtoResponse> saveStockMemberShip(@RequestBody @Valid FindMemberDto findMemberDto, @RequestBody FindStockDto findStockDto) {
        StockMemberShipDtoResponse stockMemberShipDtoResponse = stockMemberShipService.saveStockMemberShip(findMemberDto, findStockDto);
        return Result.ok(stockMemberShipDtoResponse);
    }

    // 주식 팀에 등록된 멤버 리스트 반환
    @GetMapping("/api/membership/members/{id}")
    public Result<List<MemberDtoResponse>> findSubscribeMembers(@PathVariable("id") Long id) {
        List<MemberDtoResponse> memberDtoResponseList = stockMemberShipService.findSubscribeMembers(id);
        return Result.ok(memberDtoResponseList);
    }

    // 멤버가 등록한 주식 팀 반환
    @GetMapping("/api/membership/stocks/{id}")
    public Result<List<StockDtoResponse>> findSubscribeStocks(@PathVariable("id") Long id) {
        List<StockDtoResponse> stockDtoResponseList = stockMemberShipService.findSubscribeStock(id);
        return Result.ok(stockDtoResponseList);
    }
}