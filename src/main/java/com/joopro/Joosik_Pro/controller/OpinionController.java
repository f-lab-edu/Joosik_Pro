package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.OpinionDtoResponse;
import com.joopro.Joosik_Pro.service.OpinionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/opinion")
public class OpinionController {

    private final OpinionService opinionService;

    @PostMapping("/save")
    public Result<OpinionDtoResponse> saveOpinion(
            @RequestBody @Valid CreateOpinionDto createOpinionDto,
            @RequestParam Long memberId,
            @RequestParam Long postId) {
        OpinionDtoResponse opinionDtoResponse = opinionService.SaveOpinion(createOpinionDto, memberId, postId);
        return Result.ok(opinionDtoResponse);
    }

    @GetMapping("/member/{id}")
    public Result<List<OpinionDtoResponse>> getOpinionByMemberId(@PathVariable("id") Long id) {
        List<OpinionDtoResponse> opinionDtoResponses = opinionService.findOpinionByMemberId(id);
        return Result.ok(opinionDtoResponses);
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> deleteOpinion(@PathVariable("id") Long id) {
        opinionService.deleteOpinion(id);
        return Result.ok("deleted");
    }

    @PatchMapping("/update/{id}")
    public Result<String> updateOpinion(@PathVariable("id") Long id, @RequestParam String comment) {
        opinionService.changeOpinion(id, comment);
        return Result.ok("success");
    }

    @PostMapping("/like/{id}")
    public Result<String> likeOpinion(@PathVariable("id") Long id) {
        opinionService.press_like(id);
        return Result.ok("success");
    }

    @PostMapping("/dislike/{id}")
    public Result<String> dislikeOpinion(@PathVariable("id") Long id) {
        opinionService.press_dislike(id);
        return Result.ok("success");
    }
}