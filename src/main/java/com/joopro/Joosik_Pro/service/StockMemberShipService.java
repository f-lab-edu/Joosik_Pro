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

    public List<Member> findSubscribeMembers(Stock stock){
        return stockMemberShipRepository.findSubscribeMembers(stock);
    }

    public List<Stock> findSubscribeStock(Member member){
        return stockMemberShipRepository.findSubscribeStock(member);
    }

    @Transactional
    public void cancelStockMemberShip(Long id){
        StockMembership stockMembership = stockMemberShipRepository.findStockMemberShip(id);
        stockMembership.cancel();
    }


}
