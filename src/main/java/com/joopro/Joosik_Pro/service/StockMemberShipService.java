package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.repository.StockMemberShipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class StockMemberShipService {

    private final StockMemberShipRepository stockMemberShipRepository;

    @Transactional
    public void makeStockMemberShip(StockMembership stockMembership){
        stockMemberShipRepository.makeStockMemberShip(stockMembership);
    }

    public StockMembership findStockMemberShipById(Long id){
        return stockMemberShipRepository.findStockMemberShip(id);
    }

    public List<Member> findSubscribeMembers(Stock stock){
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeMembers(stock);
        List<Member> members = stockMembershipList.stream()
                .map(s -> s.getMember())
                .toList();
        return members;
    }

    public List<Stock> findSubscribeStock(Member member){
        List<StockMembership> stockMembershipList= stockMemberShipRepository.findSubscribeStock(member);
        List<Stock> stocks = stockMembershipList.stream()
                .map(s -> s.getStock())
                .toList();
        return stocks;
    }

    @Transactional
    public void cancelStockMemberShip(Long id){
        StockMembership stockMembership = stockMemberShipRepository.findStockMemberShip(id);
        stockMembership.cancel();
    }


}
