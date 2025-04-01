package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
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

    // 멤버 등록
    @Transactional
    public MemberDtoResponse join(CreateRequestMemberDto request){
        Member member = Member.builder()
                .name(request.getUsername())
                .email(request.getEmail())
                .build();
        memberRepository.save(member);

        MemberDtoResponse memberDtoResponse = MemberDtoResponse.of(member);
        return memberDtoResponse;
    }

    // 멤버 정보 업데이트
    @Transactional
    public void update(Long id, String name, String password, String email){
        Member member = memberRepository.findOne(id);
        member.updateMember(name, password, email);
    }

    // MemberDtoResponse List 모두 반환
    public List<MemberDtoResponse> getMembers(){
        List<Member> members = memberRepository.findAll();
        List<MemberDtoResponse> memberDtoResponseList = members.stream()
                .map(m->MemberDtoResponse.of(m))
                .toList();
        return memberDtoResponseList;
    }

    // MemberId로 MemberDTOResponse 반환
    public MemberDtoResponse findByMemberId(Long memberId){
        Member member = memberRepository.findOne(memberId);
        MemberDtoResponse memberDtoResponse = MemberDtoResponse.of(member);
        return memberDtoResponse;
    }

    // MemberID로 Entity 멤버 Entity 반환
    public Member findByMemberIdReturnEntity(Long memberId){
        return memberRepository.findOne(memberId);
    }

    // 멤버 이름으로 MemberDtoResponse List 반환
    public List<MemberDtoResponse> findMemberByName(String name){
        List<Member> members = memberRepository.findByName(name);
        List<MemberDtoResponse> memberDtoResponseList = members.stream()
                .map(m->MemberDtoResponse.of(m))
                .toList();
        return memberDtoResponseList;
    }

}
