package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.postdto.CreateSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.postdto.ReturnSingleStockPostDto;
import com.joopro.Joosik_Pro.service.ArticleService;
import com.joopro.Joosik_Pro.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SingleArticleController {

    private final ArticleService articleService;
    private final StockService stockService;

    // 개별 주식 글 저장
    @PostMapping("/api/singleArticle")
    public Result saveSingleStockPost(@RequestBody @Valid CreateSingleStockPostDto createSingleStockPostDto){
        Article article = new Article();
        article.setContent(createSingleStockPostDto.getContent());
        Stock stock = stockService.findStockByName(createSingleStockPostDto.getStockName());
        SingleStockPost singleStockPost = SingleStockPost.createSingleStockPost(article, stock);
        articleService.saveSingleStockPost(singleStockPost);
        ReturnSingleStockPostDto returnSingleStockPostDto = new ReturnSingleStockPostDto(singleStockPost.getStock().getCompany_name(), singleStockPost.getArticle().getMember().getName(), singleStockPost.getArticle().getContent());
        return new Result("success", returnSingleStockPostDto);
    }

    // 개별 주식 글 내용 검색
    @GetMapping("/api/singleArticle/search")
    public Result searchSingleStockPost(@RequestParam String keyword){
        List<SingleStockPost> singleStockPostList = articleService.findSingleStockPostByContent(keyword);
        List<ReturnSingleStockPostDto> singleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return new Result("success", singleStockPostDtos);

    }

    // 개별 주식 글 검색
    @GetMapping("/api/singleArticle/{id}")
    public Result searchSingleStockById(@PathVariable("id") Long id){
        List<SingleStockPost> singleStockPostList = articleService.findSingleStockPostByStockId(id);
        List<ReturnSingleStockPostDto> singleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return new Result("success", singleStockPostDtos);
    }


    // 개별 주식 전체 불러오기
    @GetMapping("api/singleArticle")
    public Result AllSingleStock(){
        List<SingleStockPost> singleStockPostList = articleService.findAllSingleStockPost();
        List<ReturnSingleStockPostDto> singleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return new Result("success", singleStockPostDtos);
    }



}
