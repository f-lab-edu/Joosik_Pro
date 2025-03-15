package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 멤버 전체 리스트 반환
    @GetMapping("/api/members")
    public Result members(){
        List<Member> members = memberService.getMembers();
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return new Result("success", memberDtoList);
    }

    // 멤버 아이디로 찾기
    @GetMapping("/api/members/{id}")
    public Result findMemberById(@PathVariable Long id) {
        Member member = memberService.findOne(id);
        if (member == null) {
            return new Result("fail",null);
        }
        return new Result("success", member);
    }

    // 멤버 이름으로 찾기
    @GetMapping("/api/members/name")
    public Result findMemberByName(@RequestParam String name) {
        List<Member> members = memberService.findByName(name);
        List<MemberDto> memberDtoList = members.stream()
                .map(m-> new MemberDto(m.getName()))
                .toList();
        return new Result("success", memberDtoList);
    }

    // 멤버 저장
    @PostMapping("/api/members")
    public Result saveMember(@RequestBody @Valid CreateRequestMemberDto request){
        Member member = Member.createMember(request.getUsername(), request.getPassword(), request.getEmail());
        Long id = memberService.join(member);
        MemberDto returnMemberDto = new MemberDto(member.getName());
        return new Result("success", returnMemberDto);
    }


}
