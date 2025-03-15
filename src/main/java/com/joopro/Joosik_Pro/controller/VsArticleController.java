package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.postdto.CreateVsStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.ReturnSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.ReturnVsStockPostDto;
import com.joopro.Joosik_Pro.service.ArticleService;
import com.joopro.Joosik_Pro.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VsArticleController {

    private final ArticleService articleService;
    private final StockService stockService;

    @PostMapping("/api/vsArticle")
    public Result saveVsStockPost(@RequestBody @Valid CreateVsStockPostDto createVsStockPostDto){
        Article article = new Article();
        article.setContent(createVsStockPostDto.getContent());
        Stock firstStock = stockService.findStockByName(createVsStockPostDto.getFirstStockName());
        Stock secondStock = stockService.findStockByName(createVsStockPostDto.getSecondStockName());
        VsStockPost vsStockPost = VsStockPost.createVsStockPost(firstStock, secondStock, article);
        articleService.saveVsStockPost(vsStockPost);
        ReturnVsStockPostDto returnVsStockPostDto = new ReturnVsStockPostDto(firstStock.getCompany_name(), secondStock.getCompany_name(), vsStockPost.getArticle().getMember().getName(), vsStockPost.getArticle().getContent());
        return new Result("success", returnVsStockPostDto);
    }


    @GetMapping("/api/vsArticle/search")
    public Result searchVsStockPost(@RequestParam String keyword){
        List<VsStockPost> vsStockPostList = articleService.findVsStockPostByContent(keyword);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return new Result("success", vsStockPostDtos);
    }

    @GetMapping("/api/vsArticle/{id}")
    public Result searchVsStockById(@PathVariable("id")Long id){
        VsStockPost vsStockPost = articleService.findVsStockPostByPostId(id);
        ReturnVsStockPostDto returnVsStockPostDto = new ReturnVsStockPostDto(vsStockPost.getStock1().getCompany_name(), vsStockPost.getStock2().getCompany_name(),vsStockPost.getArticle().getMember().getName(), vsStockPost.getArticle().getContent());
        return new Result("success", returnVsStockPostDto);
    }

    @GetMapping("/api/vsArticle")
    public Result AllVsStock(){
        List<VsStockPost> vsStockPostList = articleService.findAllVsStockPost();
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return new Result("success", vsStockPostDtos);
    }


}
