package com.joopro.Joosik_Pro.service;

import com.joopro.Joosik_Pro.domain.Article;
import com.joopro.Joosik_Pro.domain.SingleStockPost;
import com.joopro.Joosik_Pro.domain.Stock;
import com.joopro.Joosik_Pro.dto.postdto.CreateSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.postdto.ReturnSingleStockPostDto;
import com.joopro.Joosik_Pro.dto.stockdto.StockDto;
import com.joopro.Joosik_Pro.repository.SingleStockPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @Transactional(readOnly = true)
@RequiredArgsConstructor
public class SingleArticleService {

    private final SingleStockPostRepository singleStockPostRepository;
    private final StockService stockService;

    @Transactional
    public ReturnSingleStockPostDto saveSingleStockPost(CreateSingleStockPostDto createSingleStockPostDto){
        Article article = new Article();
        article.setContent(createSingleStockPostDto.getContent());
        Stock stock = stockService.findStockByCompanyNameReturnEntity(createSingleStockPostDto.getStockName());
        SingleStockPost singleStockPost = SingleStockPost.createSingleStockPost(article, stock);
        singleStockPostRepository.save(singleStockPost);
        ReturnSingleStockPostDto returnSingleStockPostDto = new ReturnSingleStockPostDto(singleStockPost.getStock().getCompany_name(), singleStockPost.getArticle().getMember().getName(), singleStockPost.getArticle().getContent());
        return returnSingleStockPostDto;
    }

    public List<ReturnSingleStockPostDto> findSingleStockPostByStockId(Long stockId){
        List<SingleStockPost> singleStockPostList = singleStockPostRepository.findByStockId(stockId);
        List<ReturnSingleStockPostDto> returnSingleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return returnSingleStockPostDtos;
    }

    public ReturnSingleStockPostDto findSingleStockPostByPostId(Long postId){
        SingleStockPost singleStockPost = singleStockPostRepository.findById(postId);
        ReturnSingleStockPostDto returnSingleStockPostDto = new ReturnSingleStockPostDto(singleStockPost.getStock().getCompany_name(), singleStockPost.getArticle().getMember().getName(), singleStockPost.getArticle().getContent());
        return returnSingleStockPostDto;
    }

    public SingleStockPost findSingleStockPostByPostIdReturnEntity(Long postId){
        return singleStockPostRepository.findById(postId);
    }

    public List<ReturnSingleStockPostDto> findAllSingleStockPost(){
        List<SingleStockPost> singleStockPostList = singleStockPostRepository.findAllSingleStockPost();
        List<ReturnSingleStockPostDto> singleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return singleStockPostDtos;
    }

    public List<ReturnSingleStockPostDto> findSingleStockPostByContent(String keyword){
        List<SingleStockPost> singleStockPostList = singleStockPostRepository.findBySimilarContent(keyword);
        List<ReturnSingleStockPostDto> returnSingleStockPostDtos = singleStockPostList.stream()
                .map(s -> new ReturnSingleStockPostDto(s.getStock().getCompany_name(), s.getArticle().getMember().getName(), s.getArticle().getContent()))
                .toList();
        return returnSingleStockPostDtos;

    }

    @Transactional
    public void changeSingleStockPost(Long id, Article article, Stock stock){
        SingleStockPost singleStockPost = singleStockPostRepository.findById(id);
        singleStockPost.setStock(stock);
        singleStockPost.setArticle(article);
    }


}
