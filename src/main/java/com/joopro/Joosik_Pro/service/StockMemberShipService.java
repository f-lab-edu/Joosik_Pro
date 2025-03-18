package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.dto.StockMemberShipDto;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
import com.joopro.Joosik_Pro.dto.stockdto.MakeStockDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDto;
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
    public StockMemberShipDto saveStockMemberShip(MakeStockDto makeStockDto, CreateRequestMemberDto createRequestMemberDto){
        Stock stock = Stock.createStock(makeStockDto.getCompanyName(), makeStockDto.getTicker(), makeStockDto.getTicker());
        Member member = Member.createMember(createRequestMemberDto.getUsername(), createRequestMemberDto.getPassword(), createRequestMemberDto.getEmail());
        StockMembership stockMembership = StockMembership.createStockMemberShip(member, stock);
        stockMemberShipRepository.makeStockMemberShip(stockMembership);
        StockMemberShipDto stockMemberShipDto = new StockMemberShipDto(stockMembership.getMember().getName(), stockMembership.getStock().getCompany_name());
        return stockMemberShipDto;
    }

    public StockMembership findStockMemberShipById(Long id){
        return stockMemberShipRepository.findStockMemberShip(id);
    }

    public List<MemberDto> findSubscribeMembers(Long id){
        Stock stock = stockService.findStockByIdReturnEntity(id);
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeMembers(stock);
        List<Member> members = stockMembershipList.stream()
                .map(s -> s.getMember())
                .toList();
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return memberDtoList;
    }

    public List<StockDto> findSubscribeStock(Long id){
        Member member = memberService.findByMemberIdReturnEntity(id);
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeStock(member);
        List<Stock> stockList = stockMembershipList.stream()
                .map(s -> s.getStock())
                .toList();
        List<StockDto> stockDtoList = stockList.stream()
                .map(m -> new StockDto(m.getCompany_name(), m.getMember_number(), m.getArticle_number(), m.getTicker(), m.getSector()))
                .toList();
        return stockDtoList;
    }

    @Transactional
    public void cancelStockMemberShip(Long id){
        StockMembership stockMembership = stockMemberShipRepository.findStockMemberShip(id);
        stockMembership.cancel();
    }


}
