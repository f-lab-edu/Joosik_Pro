package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.StockMemberShipDto;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
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
    private final StockService stockService;
    private final MemberService memberService;

    //Stock 팀 등록
    @PostMapping("/api/membership")
    public Result saveStockMemberShip(@RequestBody @Valid MakeStockDto makeStockDto, @RequestBody CreateRequestMemberDto createRequestMemberDto){
        Stock stock = Stock.createStock(makeStockDto.getCompanyName(), makeStockDto.getTicker(), makeStockDto.getTicker());
        Member member = Member.createMember(createRequestMemberDto.getUsername(), createRequestMemberDto.getPassword(), createRequestMemberDto.getEmail());
        StockMembership stockMembership = StockMembership.createStockMemberShip(member, stock);
        stockMemberShipService.makeStockMemberShip(stockMembership);
        StockMemberShipDto stockMemberShipDto = new StockMemberShipDto(stockMembership.getMember().getName(), stockMembership.getStock().getCompany_name());
        return new Result("success", stockMemberShipDto);
    }

    // 주식 팀에 등록된 멤버 리스트 반환
    @GetMapping("/api/membership/members/{id}")
    public Result findSubscribeMembers(@PathVariable("id") Long id){
        Stock stock = stockService.findStockById(id);
        List<Member> members = stockMemberShipService.findSubscribeMembers(stock);
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return new Result("success", memberDtoList);
    }

    // 멤버가 등록한 주식 팀 반환
    @GetMapping("/api/membership/stocks/{id}")
    public Result findSubscribeStocks(@PathVariable("id") Long id){
        Member member = memberService.findOne(id);
        List<Stock> stockList = stockMemberShipService.findSubscribeStock(member);
        List<StockDto> stockDtoList = stockList.stream()
                .map(m -> new StockDto(m.getCompany_name(), m.getMember_number(), m.getArticle_number(), m.getTicker(), m.getSector()))
                .toList();
        return new Result("success", stockDtoList);
    }






}
