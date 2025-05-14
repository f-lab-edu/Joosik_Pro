package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.dto.logindto.LoginResponseDto;
import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import com.joopro.Joosik_Pro.repository.ChatRoomRepository;
import com.joopro.Joosik_Pro.repository.MemberRepository;
import com.joopro.Joosik_Pro.service.ChatService.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final ChatRoomService chatRoomService;
    private final ChatRoomRepository chatRoomRepository;

    //로그인 로직
    public LoginResponseDto login(String name, String password) {
        List<Member> members = memberRepository.findByName(name);
        if (members.isEmpty()) {
            return LoginResponseDto.fail();
        }

        // name은 중복 가능성 있으므로 첫 번째 일치 항목 기준
        for (Member member : members) {
            if (member.getPassword().equals(password)) {
                Long userId = member.getId();
                List<String> userJoinedRooms = chatRoomRepository.findRoomKeysByUser(userId);
                for (String roomId : userJoinedRooms) {
                    chatRoomService.subscribe(roomId);
                }
                return LoginResponseDto.success(MemberDtoResponse.of(member));
            }
        }
        return LoginResponseDto.fail();
    }

    // 멤버 등록
    @Transactional
    public MemberDtoResponse join(CreateRequestMemberDto request){
        Member member = Member.builder()
                .name(request.getUsername())
                .email(request.getEmail())
                .password(request.getPassword())
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
        return members.stream()
                .map(MemberDtoResponse::of)
                .toList();
    }

    // MemberId로 MemberDTOResponse 반환
    public MemberDtoResponse findByMemberId(Long memberId){
        Member member = memberRepository.findOne(memberId);
        return MemberDtoResponse.of(member);
    }

    // MemberID로 Entity 멤버 Entity 반환
    public Member findByMemberIdReturnEntity(Long memberId){
        return memberRepository.findOne(memberId);
    }

    // 멤버 이름으로 MemberDtoResponse List 반환
    public List<MemberDtoResponse> findMemberByName(String name){
        List<Member> members = memberRepository.findByName(name);
        return members.stream()
                .map(MemberDtoResponse::of)
                .toList();
    }

}