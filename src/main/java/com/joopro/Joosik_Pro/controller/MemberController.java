package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.memberdto.CreateRequestMemberDto;
import com.joopro.Joosik_Pro.dto.memberdto.MemberDtoResponse;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 멤버 전체 리스트 반환
    @GetMapping("/api/members")
    public Result<List<MemberDtoResponse>> members(){
        List<MemberDtoResponse> memberDtoResponseList = memberService.getMembers();
        return Result.ok(memberDtoResponseList);
    }

    // 멤버 아이디로 찾기
    @GetMapping("/api/members/{id}")
    public Result<MemberDtoResponse> findMemberById(@PathVariable Long id) {
        MemberDtoResponse memberDtoResponse = memberService.findByMemberId(id);
        if (memberDtoResponse == null) {
            return Result.of(HttpStatus.NOT_FOUND, "Member not found", null);
        }
        return Result.ok(memberDtoResponse);
    }

    // 멤버 이름으로 찾기
    @GetMapping("/api/members/name")
    public Result<List<MemberDtoResponse>> findMemberByName(@RequestParam String name) {
        List<MemberDtoResponse> memberDtoResponseList = memberService.findMemberByName(name);
        return Result.ok(memberDtoResponseList);
    }

    // 멤버 저장
    @PostMapping("/api/members")
    public Result<MemberDtoResponse> saveMember(@RequestBody @Valid CreateRequestMemberDto request){
        MemberDtoResponse returnMemberDtoResponse = memberService.join(request);
        return Result.ok(returnMemberDtoResponse);
    }
}
