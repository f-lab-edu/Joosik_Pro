package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public MemberDto join(CreateRequestMemberDto request){
        Member member = Member.builder()
                .name(request.getUsername())
                .email(request.getEmail())
                .build();
        memberRepository.save(member);
        MemberDto memberDto = MemberDto.builder()
                .name(member.getName())
                .build();
        return memberDto;
    }

    @Transactional
    public void update(Long id, String name, String password, String email){
        Member member = memberRepository.findOne(id);
        member.updateMember(name, password, email);
    }

    public List<MemberDto> getMembers(){
        List<Member> members = memberRepository.findAll();
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> MemberDto.builder().name(m.getName()).build())
                .toList();
        return memberDtoList;
    }

    public MemberDto findByMemberId(Long memberId){
        Member member = memberRepository.findOne(memberId);
        MemberDto memberDto = MemberDto.builder()
                .name(member.getName())
                .build();
        return memberDto;
    }

    public Member findByMemberIdReturnEntity(Long memberId){
        return memberRepository.findOne(memberId);
    }

    public List<MemberDto> findMemberByName(String name){
        List<Member> members = memberRepository.findByName(name);
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> MemberDto.builder().name(m.getName()).build())
                .toList();
        return memberDtoList;
    }

}
