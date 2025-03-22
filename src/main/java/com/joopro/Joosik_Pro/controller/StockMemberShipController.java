package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.StockMemberShipDto;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.FindMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
import com.joopro.Joosik_Pro.dto.stockdto.FindStockDto;
import com.joopro.Joosik_Pro.dto.stockdto.MakeStockDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDto;
import com.joopro.Joosik_Pro.service.MemberService;
import com.joopro.Joosik_Pro.service.StockMemberShipService;
import com.joopro.Joosik_Pro.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class StockMemberShipController {

    private final StockMemberShipService stockMemberShipService;

    //Stock 팀 등록
    @PostMapping("/api/membership")
    public Result saveStockMemberShip(@RequestBody @Valid FindMemberDto findMemberDto, @RequestBody FindStockDto findStockDto){
        StockMemberShipDto stockMemberShipDto = stockMemberShipService.saveStockMemberShip(findMemberDto, findStockDto);
        return new Result("success", stockMemberShipDto);
    }

    // 주식 팀에 등록된 멤버 리스트 반환
    @GetMapping("/api/membership/members/{id}")
    public Result findSubscribeMembers(@PathVariable("id") Long id){
        List<MemberDto> memberDtoList = stockMemberShipService.findSubscribeMembers(id);
        return new Result("success", memberDtoList);
    }

    // 멤버가 등록한 주식 팀 반환
    @GetMapping("/api/membership/stocks/{id}")
    public Result findSubscribeStocks(@PathVariable("id") Long id){
        List<StockDto> stockDtoList = stockMemberShipService.findSubscribeStock(id);
        return new Result("success", stockDtoList);
    }


}
