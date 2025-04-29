package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Post.Post;
import com.joopro.Joosik_Pro.domain.Post.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.Post.VsStockPost;
import com.joopro.Joosik_Pro.dto.postdto.CreateSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.CreateVsStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.SingleStockPostDtoResponse;
import com.joopro.Joosik_Pro.dto.postdto.VsStockPostDtoResponse;
import com.joopro.Joosik_Pro.repository.PostRepository;
import com.joopro.Joosik_Pro.service.StockService.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final StockService stockService;
    private final MemberService memberService;

    // postId로 post 찾기
    public Post findPostByPostId(Long postId){
        return postRepository.findById(postId);
    }

    // SingleStockPost 저장하고 SingleStockPostDto 반환
    @Transactional
    public SingleStockPostDtoResponse saveSingleStockPost(CreateSingleStockPostDto dto) {
        Stock stock = stockService.findStockByIdReturnEntity(dto.getStockId());
        Member member = memberService.findByMemberIdReturnEntity(dto.getUserId());
        SingleStockPost singleStockPost = SingleStockPost.makeSingleStockPost(dto.getContent(), member, stock);
        postRepository.save(singleStockPost);
        return SingleStockPostDtoResponse.of(singleStockPost);
    }

    // stockId로 SingleStockPostDto List 가져오기
    public List<SingleStockPostDtoResponse> findSingleStockPostByStockId(Long stockId) {
        return postRepository.findSingleStockPostByStockId(stockId).stream()
                .map(s -> SingleStockPostDtoResponse.of(s))
                .toList();
    }

    // VsStockPost 저장하고 VsStockPostDto 반환
    @Transactional
    public VsStockPostDtoResponse saveVsStockPost(CreateVsStockPostDto dto) {
        Member member = memberService.findByMemberIdReturnEntity(dto.getUserId());
        Stock firstStock = stockService.findStockByIdReturnEntity(dto.getFirstStockId());
        Stock secondStock = stockService.findStockByIdReturnEntity(dto.getSecondStockId());
        VsStockPost vsStockPost = VsStockPost.makeVsStockPost(dto.getContent(), member, firstStock, secondStock);
        postRepository.save(vsStockPost);
        return VsStockPostDtoResponse.of(vsStockPost);
    }

    // stockId로 VsStockPostDto List 가져오기
    public List<VsStockPostDtoResponse> findVsStockPostByStockIds(Long stockId1, Long stockId2) {
        return postRepository.findVsStockPostByStockIds(stockId1, stockId2).stream()
                .map(v -> VsStockPostDtoResponse.of(v))
                .toList();
    }

    @Transactional
    public void changeStockPost(Long id, String content){
        Post post = postRepository.findById(id);
        post.setContent(content);
    }

    public List<Post> findBySimilarContent(String keyword) {
        return postRepository.findBySimilarContent(keyword);
    }

    public List<SingleStockPostDtoResponse> findSingleStockPostBySimilarContent(String keyword) {
        return postRepository.findSingleStockPostBySimilarContent(keyword).stream()
                .map(SingleStockPostDtoResponse::of)
                .toList();
    }

    public List<VsStockPostDtoResponse> findVsStockPostBySimilarContent(String keyword) {
        return postRepository.findVsStockPostBySimilarContent(keyword).stream()
                .map(VsStockPostDtoResponse::of)
                .toList();
    }

    public List<Post> getPopularArticles() {
        return postRepository.getPopularArticles();
    }

    public List<SingleStockPostDtoResponse> getPopularSingleStockPosts() {
        return postRepository.getPopularSingleStockPosts().stream()
                .map(SingleStockPostDtoResponse::of)
                .toList();
    }

    public List<VsStockPostDtoResponse> getPopularVsStockPosts() {
        return postRepository.getPopularVsStockPosts().stream()
                .map(VsStockPostDtoResponse::of)
                .toList();
    }

}
