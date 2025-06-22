package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SaveService {

    private final FirstComeEventRepositoryV1 eventRepositoryV1;
    private final StockRepository stockRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveParticipants(Long eventId, List<Long> orderedList) {
        Stock stock = stockRepository.findStockById(eventId);
        int a = 0;
        for (Long memberId : orderedList) {
            Member member = memberRepository.findOne(memberId);

            FirstComeEventParticipation participation = FirstComeEventParticipation.builder()
                    .member(member)
                    .stock(stock)
                    .participateOrder(a++)
                    .build();

            eventRepositoryV1.makefirstcomeevent(participation);
        }
    }
}
