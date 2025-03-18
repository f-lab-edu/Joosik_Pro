package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
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

    @Transactional
    public ReturnVsStockPostDto saveVsStockPost(CreateVsStockPostDto createVsStockPostDto){
        Article article = new Article();
        article.setContent(createVsStockPostDto.getContent());
        Stock firstStock = stockService.findStockByCompanyNameReturnEntity(createVsStockPostDto.getFirstStockName());
        Stock secondStock = stockService.findStockByCompanyNameReturnEntity(createVsStockPostDto.getSecondStockName());
        VsStockPost vsStockPost = VsStockPost.createVsStockPost(firstStock, secondStock, article);
        vsStockPostRepository.save(vsStockPost);
        ReturnVsStockPostDto returnVsStockPostDto = new ReturnVsStockPostDto(firstStock.getCompany_name(), secondStock.getCompany_name(), vsStockPost.getArticle().getMember().getName(), vsStockPost.getArticle().getContent());
        return returnVsStockPostDto;
    }

    public List<ReturnVsStockPostDto> findVsStockPostByStockIds(Long stockId1, Long stockId2) {
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findVsStockPostByStockIds(stockId1, stockId2);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return vsStockPostDtos;
    }

    public List<ReturnVsStockPostDto> findVsStockPostByBelongSinglePostId(Long stockId){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findVsStockPostByBelongSinglePostId(stockId);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return vsStockPostDtos;
    }


    public ReturnVsStockPostDto findVsStockPostByPostId(Long postId){
        VsStockPost vsStockPost = vsStockPostRepository.findByVsStockPostId(postId);
        ReturnVsStockPostDto returnVsStockPostDto = new ReturnVsStockPostDto(vsStockPost.getStock1().getCompany_name(), vsStockPost.getStock2().getCompany_name(),vsStockPost.getArticle().getMember().getName(), vsStockPost.getArticle().getContent());
        return returnVsStockPostDto;
    }

    public VsStockPost findVsStockPostByPostIdReturnEntity(Long postId){
        return vsStockPostRepository.findByVsStockPostId(postId);
    }

    public List<ReturnVsStockPostDto> findAllVsStockPost(){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findAllVsStockPost();
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return vsStockPostDtos;

    }

    public List<ReturnVsStockPostDto> findVsStockPostByContent(String keyword){
        List<VsStockPost> vsStockPostList = vsStockPostRepository.findBySimilarContent(keyword);
        List<ReturnVsStockPostDto> vsStockPostDtos = vsStockPostList.stream()
                .map(s -> new ReturnVsStockPostDto(s.getStock1().getCompany_name(), s.getStock1().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return vsStockPostDtos;
    }

    @Transactional
    public void changeVsStockPost(Long id, Article article, Stock stock1, Stock stock2){
        VsStockPost vsStockPost = vsStockPostRepository.findByVsStockPostId(id);
        vsStockPost.setArticle(article);
        vsStockPost.setStock1(stock1);
        vsStockPost.setStock2(stock2);
    }

}
