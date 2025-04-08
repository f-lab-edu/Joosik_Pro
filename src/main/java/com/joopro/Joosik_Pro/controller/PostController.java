package com.joopro.Joosik_Pro.controller;

import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.dto.Result;
import com.joopro.Joosik_Pro.dto.postdto.CreateSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.CreateVsStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.SingleStockPostDtoResponse;
import com.joopro.Joosik_Pro.dto.postdto.VsStockPostDtoResponse;
import com.joopro.Joosik_Pro.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/single")
    public Result<SingleStockPostDtoResponse> saveSingleStockPost(@RequestBody @Valid CreateSingleStockPostDto createSingleStockPostDto) {
        SingleStockPostDtoResponse response = postService.saveSingleStockPost(createSingleStockPostDto);
        return Result.ok(response);
    }

    @PostMapping("/vs")
    public Result<VsStockPostDtoResponse> saveVsStockPost(@RequestBody @Valid CreateVsStockPostDto createVsStockPostDto) {
        VsStockPostDtoResponse response = postService.saveVsStockPost(createVsStockPostDto);
        return Result.ok(response);
    }

    @GetMapping("/single/search")
    public Result<List<SingleStockPostDtoResponse>> searchSingleStockPost(@RequestParam String keyword) {
        List<SingleStockPostDtoResponse> responses = postService.findSingleStockPostBySimilarContent(keyword);
        return Result.ok(responses);
    }

    @GetMapping("/vs/search")
    public Result<List<VsStockPostDtoResponse>> searchVsStockPost(@RequestParam String keyword) {
        List<VsStockPostDtoResponse> responses = postService.findVsStockPostBySimilarContent(keyword);
        return Result.ok(responses);
    }

    @GetMapping("/post/{id}")
    public Result<Post> searchStockByPostId(@PathVariable("id") Long id) {
        Post postByPostId = postService.findPostByPostId(id);
        return Result.ok(postByPostId);
    }


    @GetMapping("/single/stock/{id}")
    public Result<List<SingleStockPostDtoResponse>> searchSingleStockByStockId(@PathVariable("id") Long id) {
        List<SingleStockPostDtoResponse> singleStockPostByStockId = postService.findSingleStockPostByStockId(id);
        return Result.ok(singleStockPostByStockId);
    }

    @GetMapping("/vs/stock")
    public Result<List<VsStockPostDtoResponse>> searchVsStockByStockIds(
            @RequestParam("id1") Long id1,
            @RequestParam("id2") Long id2) {
        List<VsStockPostDtoResponse> responses = postService.findVsStockPostByStockIds(id1, id2);
        return Result.ok(responses);
    }


}
