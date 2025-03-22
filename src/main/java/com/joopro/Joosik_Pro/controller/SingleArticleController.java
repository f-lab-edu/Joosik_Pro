package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.dto.postdto.CreateSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.postdto.ReturnSingleStockPostDto;
import com.joopro.Joosik_Pro.service.SingleArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SingleArticleController {

    private final SingleArticleService singleArticleService;

    // 개별 주식 글 저장
    @PostMapping("/api/singleArticle")
    public Result saveSingleStockPost(@RequestBody @Valid CreateSingleStockPostDto createSingleStockPostDto){
        ReturnSingleStockPostDto returnSingleStockPostDto = singleArticleService.saveSingleStockPost(createSingleStockPostDto);
        return new Result("success", returnSingleStockPostDto);
    }

    // 개별 주식 글 내용 검색
    @GetMapping("/api/singleArticle/search")
    public Result searchSingleStockPost(@RequestParam String keyword){
        List<ReturnSingleStockPostDto> returnSingleStockPostDtos = singleArticleService.findSingleStockPostByContent(keyword);
        return new Result("success", returnSingleStockPostDtos);

    }

    // 개별 주식 글 ID로 검색
    @GetMapping("/api/singleArticle/{id}")
    public Result searchSingleStockByPostId(@PathVariable("id") Long id){
        ReturnSingleStockPostDto returnSingleStockPostDto = singleArticleService.findSingleStockPostByPostId(id);
        return new Result("success", returnSingleStockPostDto);
    }


    // 관련 주식 ID로 검색
    @GetMapping("/api/singleArticle/stock/{id}")
    public Result searchSingleStockByStockId(@PathVariable("id") Long id){
        List<ReturnSingleStockPostDto> returnSingleStockPostDtos = singleArticleService.findSingleStockPostByStockId(id);
        return new Result("success", returnSingleStockPostDtos);
    }


    // 개별 주식 전체 불러오기
    @GetMapping("api/singleArticle")
    public Result FindAllSingleStockPost(){
        List<ReturnSingleStockPostDto> singleStockPostDtos = singleArticleService.findAllSingleStockPost();
        return new Result("success", singleStockPostDtos);
    }
}
