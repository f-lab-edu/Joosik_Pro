package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.postdto.CreateVsStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.ReturnVsStockPostDto;
import com.joopro.Joosik_Pro.service.StockService;
import com.joopro.Joosik_Pro.service.VsArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VsArticleController {

    private final VsArticleService vsArticleService;

    // vs 주식 글 저장
    @PostMapping("/api/vsArticle")
    public Result saveVsStockPost(@RequestBody @Valid CreateVsStockPostDto createVsStockPostDto){
        ReturnVsStockPostDto returnVsStockPostDto = vsArticleService.saveVsStockPost(createVsStockPostDto);
        return new Result("success", returnVsStockPostDto);
    }

    // vs 주식 글 내용 검색
    @GetMapping("/api/vsArticle/search")
    public Result searchVsStockPost(@RequestParam String keyword){
        List<ReturnVsStockPostDto> vsStockPostDtos = vsArticleService.findVsStockPostByContent(keyword);
        return new Result("success", vsStockPostDtos);
    }

    // vs 주식 글 ID로 검색
    @GetMapping("/api/vsArticle/{id}")
    public Result searchVsStockById(@PathVariable("id")Long id){
        ReturnVsStockPostDto returnVsStockPostDto = vsArticleService.findVsStockPostByPostId(id);
        return new Result("success", returnVsStockPostDto);
    }


    // vs 주식 전체 불러오기
    @GetMapping("/api/vsArticle")
    public Result AllVsStock(){
        List<ReturnVsStockPostDto> vsStockPostDtos = vsArticleService.findAllVsStockPost();
        return new Result("success", vsStockPostDtos);
    }

    // vs 주식 1개 포함 글 불러오기
    @GetMapping("/api/vsArticle/search")
    public Result findVsStockPostByBelongSinglePostId(@RequestParam("id") Long id){
        List<ReturnVsStockPostDto> vsStockPostDtos = vsArticleService.findVsStockPostByBelongSinglePostId(id);
        return new Result("success", vsStockPostDtos);
    }

    // vs 주식 2개 포함 글 불러오기
    @GetMapping("/api/vs/article/search")
    public Result findVsStockPostByStockIds(@RequestParam("id") Long id, @RequestParam("id2") Long id2){
        List<ReturnVsStockPostDto> vsStockPostDtos = vsArticleService.findVsStockPostByStockIds(id, id2);
        return new Result("Success", vsStockPostDtos);
    }


}
