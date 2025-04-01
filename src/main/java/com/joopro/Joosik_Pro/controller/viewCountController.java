package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import com.joopro.Joosik_Pro.dto.PostDtoResponse;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.service.ViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class viewCountController {

    private final ViewCountService viewCountService;

    @PostMapping("/api/singleArticle/{id}/increaseView")
    public Result increaseSingleArticleViewCount(@PathVariable("id") Long id){
        viewCountService.increaseSingleStockPostViewCount(id);
        return new Result("success", new String("success"));
    }

    @PostMapping("/api/vsArticle/{id}/increaseView")
    public Result increaseVsArticleViewCount(@PathVariable("id") Long id){
        viewCountService.increaseVsStockPostViewCount(id);
        return new Result("success", new String("success"));
    }

    @GetMapping("/api/singleArticle/findPopularArticle")
    public Result findPopularSingleArticle(){
        List<SingleStockPost> singleStockPostList = viewCountService.findPopularSingleStockPostArticle();
        List<Article> articleList = singleStockPostList.stream()
                .map(a-> a.getArticle())
                .toList();
        List<PostDtoResponse> postDtoResponseList = articleList.stream()
                .map(a -> new PostDtoResponse(a.getMember().getName(), a.getContent()))
                .toList();
        return new Result("success", postDtoResponseList);

    }

    @GetMapping("/api/vsArticle/findPopularArticle")
    public Result findPopularVsArticle(){
        List<VsStockPost> vsStockPostList = viewCountService.findPopularVsStockPostArticle();
        List<Article> articleList = vsStockPostList.stream()
                .map(a-> a.getArticle())
                .toList();
        List<PostDtoResponse> postDtoResponseList = articleList.stream()
                .map(a -> new PostDtoResponse(a.getMember().getName(), a.getContent()))
                .toList();
        return new Result("success", postDtoResponseList);

    }

}
