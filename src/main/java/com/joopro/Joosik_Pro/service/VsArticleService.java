package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.Member;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.domain.VsStockPost;
import com.joopro.Joosik_Pro.dto.postdto.CreateVsStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.ReturnVsStockPostDto;
import com.joopro.Joosik_Pro.repository.VsStockPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VsArticleService {

    private final VsStockPostRepository vsStockPostRepository;
    private final StockService stockService;
    private final MemberService memberService;

    @Transactional
    public ReturnVsStockPostDto saveVsStockPost(CreateVsStockPostDto createVsStockPostDto){
        Member member = memberService.findByMemberIdReturnEntity(createVsStockPostDto.getUserId());
        Article article = Article.createArticle(createVsStockPostDto.getContent(), member);
        Stock firstStock = stockService.findStockByCompanyNameReturnEntity(createVsStockPostDto.getFirstStockName());
        Stock secondStock = stockService.findStockByCompanyNameReturnEntity(createVsStockPostDto.getSecondStockName());
        VsStockPost vsStockPost = article.createVsStockPost(article, firstStock, secondStock);
        vsStockPostRepository.save(vsStockPost);
        ReturnVsStockPostDto returnVsStockPostDto = ReturnVsStockPostDto.builder()
                .firstStockName(firstStock.getCompanyName())
                .secondStockName(secondStock.getCompanyName())
                .content(vsStockPost.getArticle().getMember().getName())
                .memberName(vsStockPost.getArticle().getContent())
                .build();
        return returnVsStockPostDto;
    }

    public List<ReturnVsStockPostDto> findVsStockPostByStockIds(Long stockId1, Long stockId2) {
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findVsStockPostByStockIds(stockId1, stockId2);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> ReturnVsStockPostDto.builder()
                        .firstStockName(s.getStock1().getCompanyName())
                        .secondStockName(s.getStock2().getCompanyName())
                        .memberName(s.getArticle().getMember().getName())
                        .content(s.getArticle().getContent())
                        .build())
                .toList();
        return vsStockPostDtos;
    }

    public List<ReturnVsStockPostDto> findVsStockPostByBelongSinglePostId(Long stockId){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findVsStockPostByBelongSinglePostId(stockId);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> ReturnVsStockPostDto.builder()
                        .firstStockName(s.getStock1().getCompanyName())
                        .secondStockName(s.getStock2().getCompanyName())
                        .memberName(s.getArticle().getMember().getName())
                        .content(s.getArticle().getContent())
                        .build())
                .toList();
        return vsStockPostDtos;
    }


    public ReturnVsStockPostDto findVsStockPostByPostId(Long postId){
        VsStockPost vsStockPost = vsStockPostRepository.findByVsStockPostId(postId);
        ReturnVsStockPostDto returnVsStockPostDto = ReturnVsStockPostDto.builder()
                .firstStockName(vsStockPost.getStock1().getCompanyName())
                .secondStockName(vsStockPost.getStock2().getCompanyName())
                .content(vsStockPost.getArticle().getMember().getName())
                .memberName(vsStockPost.getArticle().getContent())
                .build();
        return returnVsStockPostDto;
    }

    public VsStockPost findVsStockPostByPostIdReturnEntity(Long postId){
        return vsStockPostRepository.findByVsStockPostId(postId);
    }

    public List<ReturnVsStockPostDto> findAllVsStockPost(){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findAllVsStockPost();
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> ReturnVsStockPostDto.builder()
                        .firstStockName(s.getStock1().getCompanyName())
                        .secondStockName(s.getStock2().getCompanyName())
                        .memberName(s.getArticle().getMember().getName())
                        .content(s.getArticle().getContent())
                        .build())
                .toList();
        return vsStockPostDtos;

    }

    public List<ReturnVsStockPostDto> findVsStockPostByContent(String keyword){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findBySimilarContent(keyword);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> ReturnVsStockPostDto.builder()
                        .firstStockName(s.getStock1().getCompanyName())
                        .secondStockName(s.getStock2().getCompanyName())
                        .memberName(s.getArticle().getMember().getName())
                        .content(s.getArticle().getContent())
                        .build())
                .toList();
        return vsStockPostDtos;
    }

    @Transactional
    public void changeVsStockPost(Long id, Article article){
        VsStockPost vsStockPost = vsStockPostRepository.findByVsStockPostId(id);
        vsStockPost.setArticle(article);
    }

}
