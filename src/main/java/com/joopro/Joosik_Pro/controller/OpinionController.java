package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.ReturnOpinionDto;
import com.joopro.Joosik_Pro.service.SingleArticleService;
import com.joopro.Joosik_Pro.service.MemberService;
import com.joopro.Joosik_Pro.service.OpinionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OpinionController {

    private final OpinionService opinionService;

    @PostMapping("/api/singleopinion")
    public Result saveSingleArticleOpinion(@RequestBody @Valid CreateOpinionDto createOpinionDto, @RequestParam Long memberId, @RequestParam Long articleId){
        ReturnOpinionDto returnOpinionDto = opinionService.saveSingleArticleOpinion(createOpinionDto, memberId, articleId);
        return new Result("success", returnOpinionDto);
    }

    @PostMapping("/api/vsopinion")
    public Result saveVsArticleOpinion(@RequestBody @Valid CreateOpinionDto createOpinionDto, @RequestParam Long memberId, @RequestParam Long articleId){
        ReturnOpinionDto returnOpinionDto = opinionService.saveVsArticleOpinion(createOpinionDto, memberId, articleId);
        return new Result("success", returnOpinionDto);

    }

    @GetMapping("/api/opinion/member/{id}")
    public Result getOpinionByMemberId(@PathVariable("id")Long id){
        List<ReturnOpinionDto> returnOpinionDtos = opinionService.findOpinionByMemberId(id);
        return new Result("success", returnOpinionDtos);
    }

}
