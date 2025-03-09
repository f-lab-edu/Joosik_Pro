package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.StockMembership;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class StockMemberShipServiceTest {

    @Autowired StockMemberShipService stockMemberShipService;
    @Autowired EntityManager em;

    @Test
    void makeStockMemberShip() {

        Member member = Member.createMember("주식", "프로", "abc");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");

        StockMembership stockMembership = StockMembership.createStockMemberShip(member, stock);

        stockMemberShipService.makeStockMemberShip(stockMembership);

        Assertions.assertThat(stockMembership).isEqualTo(stockMemberShipService.findStockMemberShipById(stockMembership.getId()));


    }

    @Test
    void findSubscribeMembers() {
        Member member1 = Member.createMember("주식1", "프로1", "abc1");
        Member member2 = Member.createMember("주식2", "프로2", "abc2");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");

        em.persist(member1);
        em.persist(member2);
        em.persist(stock);

        StockMembership stockMembership1 = StockMembership.createStockMemberShip(member1, stock);
        StockMembership stockMembership2 = StockMembership.createStockMemberShip(member2, stock);
        stockMemberShipService.makeStockMemberShip(stockMembership1);
        stockMemberShipService.makeStockMemberShip(stockMembership2);

        List<Member> memberList = stockMemberShipService.findSubscribeMembers(stock);
        Assertions.assertThat(memberList).contains(member1, member2);
        Assertions.assertThat(memberList.size()).isEqualTo(2);
    }


    @Test
    void findSubscribeStock() {
        Member member1 = Member.createMember("주식1", "프로1", "abc1");
        Stock stock1 = Stock.createStock("테슬라", "TSLA", "IT");
        Stock stock2 = Stock.createStock("엔비디아", "NVDA", "AI");

        em.persist(member1);
        em.persist(stock1);
        em.persist(stock2);

        StockMembership stockMembership1 = StockMembership.createStockMemberShip(member1, stock1);
        StockMembership stockMembership2 = StockMembership.createStockMemberShip(member1, stock2);
        stockMemberShipService.makeStockMemberShip(stockMembership1);
        stockMemberShipService.makeStockMemberShip(stockMembership2);


        List<Stock> stockList = stockMemberShipService.findSubscribeStock(member1);
        Assertions.assertThat(stockList).contains(stock1, stock2);
        Assertions.assertThat(stockList.size()).isEqualTo(2);

    }

    @Test
    void cancelStockMemberShip() {
        Member member = Member.createMember("주식", "프로", "abc");
        Stock stock = Stock.createStock("테슬라", "TSLA", "IT");

        StockMembership stockMembership = StockMembership.createStockMemberShip(member, stock);

        stockMemberShipService.makeStockMemberShip(stockMembership);
        stockMemberShipService.cancelStockMemberShip(stockMembership.getId());
        Assertions.assertThat(stockMembership.isActive()).isEqualTo(false);


    }
}