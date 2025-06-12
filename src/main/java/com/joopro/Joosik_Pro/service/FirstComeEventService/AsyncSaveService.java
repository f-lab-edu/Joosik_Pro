package com.joopro.Joosik_Pro.service.FirstComeEventService;

import com.joopro.Joosik_Pro.domain.FirstComeEventParticipation;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.repository.FirstComeEventRepository.FirstComeEventRepositoryV1;
import com.joopro.Joosik_Pro.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AsyncSaveService {

    private final FirstComeEventRepositoryV1 eventRepositoryV1;
    private final MemberRepository memberRepository;

    @Transactional
    public void saveParticipantTransactional(Stock stock, Long memberId, int order) {
        Member member = memberRepository.findOne(memberId);
        FirstComeEventParticipation participation = FirstComeEventParticipation.builder()
                .member(member)
                .stock(stock)
                .participateOrder(order)
                .build();

        eventRepositoryV1.makefirstcomeevent(participation);
    }
}
