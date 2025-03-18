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
        Member member = Member.createMember(request.getUsername(), request.getPassword(), request.getEmail());
        memberRepository.save(member);
        MemberDto returnMemberDto = new MemberDto(member.getName());
        return returnMemberDto;
    }

    @Transactional
    public void update(Long id, String name, String password, String email){
        Member member = memberRepository.findOne(id);
        member.setName(name);
        member.setPassword(password);
        member.setEmail(email);
    }

    public List<MemberDto> getMembers(){
        List<Member> members = memberRepository.findAll();
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return memberDtoList;
    }

    public MemberDto findByMemberId(Long memberId){
        Member member = memberRepository.findOne(memberId);
        MemberDto memberDto = new MemberDto(member.getName());
        return memberDto;
    }

    public Member findByMemberIdReturnEntity(Long memberId){
        return memberRepository.findOne(memberId);
    }

    public List<MemberDto> findByMemberName(String name){
        List<Member> members = memberRepository.findByName(name);
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return memberDtoList;
    }

}
