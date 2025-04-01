package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.dto.StockMemberShipDtoResponse;
import com.joopro.Joosik_Pro.dto.memberdto.FindMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import com.joopro.Joosik_Pro.dto.stockdto.FindStockDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDtoResponse;
import com.joopro.Joosik_Pro.repository.StockMemberShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockMemberShipService {

    private final StockMemberShipRepository stockMemberShipRepository;
    private final StockService stockService;
    private final MemberService memberService;

    @Transactional
    public StockMemberShipDtoResponse saveStockMemberShip(FindMemberDto findMemberDto, FindStockDto findStockDto){
        Member member = memberService.findByMemberIdReturnEntity(findMemberDto.getMemberId());
        Stock stock = stockService.findStockByIdReturnEntity(findStockDto.getStockId());
        StockMembership stockMembership = StockMembership.createStockMemberShip(member, stock);
        stockMemberShipRepository.makeStockMemberShip(stockMembership);
        StockMemberShipDtoResponse stockMemberShipDtoResponse = StockMemberShipDtoResponse.of(stockMembership);
        return stockMemberShipDtoResponse;
    }

    public StockMembership findStockMemberShipById(Long id){
        return stockMemberShipRepository.findStockMemberShip(id);
    }

    public List<MemberDtoResponse> findSubscribeMembers(Long id){
        Stock stock = stockService.findStockByIdReturnEntity(id);
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeMembers(stock);
        List<Member> members = stockMembershipList.stream()
                .map(s -> s.getMember())
                .toList();
        List<MemberDtoResponse> memberDtoResponseList = members.stream()
                .map(m -> MemberDtoResponse.of(m))
                .toList();
        return memberDtoResponseList;
    }

    public List<StockDtoResponse> findSubscribeStock(Long id){
        Member member = memberService.findByMemberIdReturnEntity(id);
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeStock(member);
        List<Stock> stockList = stockMembershipList.stream()
                .map(s -> s.getStock())
                .toList();
        List<StockDtoResponse> stockDtoResponseList = stockList.stream()
                .map(s -> StockDtoResponse.of(s))
                .toList();
        return stockDtoResponseList;
    }

    @Transactional
    public void cancelStockMemberShip(Long id){
        StockMembership stockMembership = stockMemberShipRepository.findStockMemberShip(id);
        stockMembership.cancel();
    }


}
