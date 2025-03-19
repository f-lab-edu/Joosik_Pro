package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDto;
import com.joopro.Joosik_Pro.dto.Result;
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
        List<MemberDto> memberDtoList = memberService.getMembers();
        return new Result("success", memberDtoList);
    }

    // 멤버 아이디로 찾기
    @GetMapping("/api/members/{id}")
    public Result findMemberById(@PathVariable Long id) {
        MemberDto memberDto = memberService.findByMemberId(id);
        if (memberDto == null) {
            return new Result("fail",null);
        }
        return new Result("success", memberDto);
    }

    // 멤버 이름으로 찾기
    @GetMapping("/api/members/name")
    public Result findMemberByName(@RequestParam String name) {
        List<MemberDto> memberDtoList = memberService.findMemberByName(name);
        return new Result("success", memberDtoList);
    }

    // 멤버 저장
    @PostMapping("/api/members")
    public Result saveMember(@RequestBody @Valid CreateRequestMemberDto request){
        MemberDto returnMemberDto = memberService.join(request);
        return new Result("success", returnMemberDto);
    }


}
