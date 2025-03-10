package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Opinion;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.opiniondto.CreateOpinionDto;
import com.joopro.Joosik_Pro.dto.opiniondto.ReturnOpinionDto;
import com.joopro.Joosik_Pro.service.ArticleService;
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
    private final MemberService memberService;
    private final ArticleService articleService;

    @PostMapping("/api/singleopinion")
    public Result saveOpinion(@RequestBody @Valid CreateOpinionDto createOpinionDto, @RequestParam Long memberId, @RequestParam Long articleId){
        Member member = memberService.findOne(memberId);
        Article article;
        article = articleService.findSingleStockPostByPostId(articleId).getArticle();
        Opinion opinion = Opinion.makeOpinion(createOpinionDto.getComment(), member, article);
        opinionService.saveOpinion(opinion);
        ReturnOpinionDto returnOpinionDto = new ReturnOpinionDto(memberId, articleId, opinion.getId());
        return new Result("success", returnOpinionDto);
    }

    @PostMapping("/api/vsopinion")
    public Result saveOpinion(@RequestBody @Valid CreateOpinionDto createOpinionDto, @RequestParam Long memberId, @RequestParam Long articleId, @RequestParam int check){
        Member member = memberService.findOne(memberId);
        Article article;
        article = articleService.findVsStockPostByPostId(articleId).getArticle();
        Opinion opinion = Opinion.makeOpinion(createOpinionDto.getComment(), member, article);
        opinionService.saveOpinion(opinion);
        ReturnOpinionDto returnOpinionDto = new ReturnOpinionDto(memberId, articleId, opinion.getId());
        return new Result("success", returnOpinionDto);
    }

    @GetMapping("/api/opinion/member/{id}")
    public Result getOpinionByMemberId(@PathVariable("id")Long id){
        List<Opinion> opinionList = opinionService.findByMemberId(id);
        List<ReturnOpinionDto> returnOpinionDtos = opinionList.stream()
                .map(o -> new ReturnOpinionDto(o.getMember().getId(), o.getArticle().getId(), o.getId()))
                .toList();
        return new Result("success", returnOpinionDtos);
    }







}
